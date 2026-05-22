package net.livesplitintegration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

//file parsing written by openai
public class SplitManager {

    private final LivesplitController controller;
    private final List<Split> splits = new ArrayList<>();
    private int currentSplitIndex = 0;

    public SplitManager(LivesplitController controller) {
        this.controller = controller;
        loadSplits();
    }

    private void loadSplits() {
        try {
            File file = new File("config/splits.txt");

            if (!file.exists()) {
                System.out.println("[Livesplit Integration] config/splits.txt not found");
                return;
            }

            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }
            reader.close();

            // Each split block:
            //   <name>
            //   [item name]          optional — no commas, not a plain number
            //   [x, y, z]            optional coord line
            //   [x, y, z  OR radius] optional — second coord pair (bounding box)
            //                        OR a plain number (radius mode)
            int i = 0;
            while (i < lines.size()) {
                String nameLine = lines.get(i);

                if (isCoordLine(nameLine) || isNumber(nameLine)) {
                    System.out.println("[Livesplit Integration] Unexpected line where split name expected: " + nameLine);
                    i++;
                    continue;
                }

                i++;

                // Optional item line — not a coord and not a bare number
                String requiredItem = null;
                if (i < lines.size() && !isCoordLine(lines.get(i)) && !isNumber(lines.get(i))) {
                    requiredItem = lines.get(i);
                    i++;
                }

                // Optional coordinate block
                double[] center = null;
                double[] pos2   = null;
                double   radius = -1;

                if (i < lines.size() && isCoordLine(lines.get(i))) {
                    center = parseCoords(lines.get(i));
                    i++;

                    if (i < lines.size() && isCoordLine(lines.get(i))) {
                        // Two coord lines → bounding box
                        pos2 = parseCoords(lines.get(i));
                        i++;
                    } else if (i < lines.size() && isNumber(lines.get(i))) {
                        // Coord + number → radius mode
                        radius = Double.parseDouble(lines.get(i).trim());
                        i++;
                    } else {
                        System.out.println("[Livesplit Integration] Split '" + nameLine + "' has a coord line but no second coord or radius; ignoring coords.");
                        center = null;
                    }
                }

                if (requiredItem == null && center == null) {
                    System.out.println("[Livesplit Integration] Split '" + nameLine + "' has no conditions; skipping.");
                    continue;
                }

                splits.add(new Split(nameLine, requiredItem, center, pos2, radius));

                String coordDesc = "";
                if (center != null) {
                    coordDesc = radius >= 0
                            ? " | radius " + radius + " around " + fmtCoord(center)
                            : " | " + fmtCoord(center) + " to " + fmtCoord(pos2);
                }
                System.out.println("[Livesplit Integration] '" + nameLine + "'"
                        + (requiredItem != null ? " | item: \"" + requiredItem + "\"" : "")
                        + coordDesc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** True if the line contains a comma — i.e. looks like "x, y, z". */
    private boolean isCoordLine(String line) {
        return line.contains(",");
    }

    /** True if the line is a plain integer or decimal number (the radius). */
    private boolean isNumber(String line) {
        try {
            Double.parseDouble(line.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double[] parseCoords(String line) {
        String[] parts = line.split(",");
        return new double[]{
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim()),
                Double.parseDouble(parts[2].trim())
        };
    }

    private String fmtCoord(double[] c) {
        return "(" + c[0] + ", " + c[1] + ", " + c[2] + ")";
    }


    public void handle(MinecraftClient client) {
        if (client.player == null) return;
        if (currentSplitIndex >= splits.size()) return;

        Split split = splits.get(currentSplitIndex);

        boolean coordsMet = true;
        boolean itemMet   = true;

        if (split.center != null) {
            double x = client.player.x;
            double y = client.player.y;
            double z = client.player.z;
            coordsMet = split.radius >= 0
                    ? isInRadius(x, y, z, split)
                    : isInBoundingBox(x, y, z, split);
        }

        if (split.requiredItem != null) {
            itemMet = hasItem(client, split.requiredItem);
        }

        if (coordsMet && itemMet) {
            controller.split();
            currentSplitIndex++;
        }
    }


    /** Euclidean distance check — true if the player is within split.radius blocks of split.center. */
    private boolean isInRadius(double x, double y, double z, Split split) {
        double dx = x - split.center[0];
        double dy = y - split.center[1];
        double dz = z - split.center[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= split.radius;
    }

    /** Axis-aligned bounding box between split.center and split.pos2. */
    private boolean isInBoundingBox(double x, double y, double z, Split split) {
        double minX = Math.min(split.center[0], split.pos2[0]);
        double maxX = Math.max(split.center[0], split.pos2[0]);
        double minY = Math.min(split.center[1], split.pos2[1]);
        double maxY = Math.max(split.center[1], split.pos2[1]);
        double minZ = Math.min(split.center[2], split.pos2[2]);
        double maxZ = Math.max(split.center[2], split.pos2[2]);
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }


    /** Removes Minecraft formatting codes (e.g. §a, §l) from a string. */
    private String stripFormatting(String s) {
        return s.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }


    private boolean hasItem(MinecraftClient client, String itemName) {
        if (client.player == null) return false;

        int size = client.player.inventory.getInvSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = client.player.inventory.getInvStack(i);
            if (stack != null && stripFormatting(stack.getCustomName()).equalsIgnoreCase(itemName)) {
                return true;
            }
        }
        return false;
    }


    public void reset() {
        currentSplitIndex = 0;
    }


    private static class Split {
        String   name;
        String   requiredItem; // null  → no item condition
        double[] center;       // null  → no coordinate condition
        double[] pos2;         // non-null → bounding box mode (center..pos2)
        double   radius;       // >= 0  → radius mode; -1 = bounding box mode

        Split(String name, String requiredItem, double[] center, double[] pos2, double radius) {
            this.name         = name;
            this.requiredItem = requiredItem;
            this.center       = center;
            this.pos2         = pos2;
            this.radius       = radius;
        }
    }


    public void reload() {
        splits.clear();
        currentSplitIndex = 0;
        loadSplits();
        System.out.println("[Livesplit Integration] Reloaded splits");

        // Debug: print every item in the player's inventory
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            System.out.println("[Livesplit Integration] Inventory:");
            int size = client.player.inventory.getInvSize();
            for (int i = 0; i < size; i++) {
                ItemStack stack = client.player.inventory.getInvStack(i);
                if (stack != null) {
                    System.out.println("[Livesplit Integration] Slot " + i + ": \""
                            + stripFormatting(stack.getCustomName())
                            + "\" (raw: \"" + stack.getCustomName() + "\") x" + stack.count);
                }
            }
        } else {
            System.out.println("[Livesplit Integration] Could not print inventory: player is null");
        }
    }
}