import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String[] testValues = {
                "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L",
                "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X",
                "Y", "Z"
        };

        String[][] bingo = new String[5][5];
        String pathToPoirot = "C:/Users/Goezde/Pictures/Poirot.jpg";
        assignValues(bingo, poirotContent);
        printToImage(bingo, pathToPoirot);
    }

    /**
     * assigns values from possibleValues randomly to all cells in bingo
     * no values may appear repeatedly
     * @param bingo array to be filled with values
     * @param possibleValues values that can randomly be assigned to cells of bingo. Must have at least 25 values!
     */
    private static void assignValues(String[][] bingo, String[] possibleValues) throws Exception {
        if (possibleValues.length < 25)
            throw new Exception("List of possible content must contain at least 25 values");

        // create a list that contains 25 random indices from possibleValues without repetition
        List<Integer> allIndices = new ArrayList<>();
        // add all indices
        for(int i = 0; i < possibleValues.length; i++)
            allIndices.add(i);
        // randomly shuffle the indices
        Collections.shuffle(allIndices, new Random());
        // use first 25 indices
        List<Integer> randomIndices = allIndices.subList(0, 25);

        // write strings at random indices into bingo
        for (int i=0; i<5; i++)
            for (int j=0; j<5; j++)
                bingo[i][j] = possibleValues[randomIndices.get(i*5 + j)];

        bingo[2][2] = "";
    }

    /**
     * exports input array as image of a grid to desktop
     * @param bingo values to be exported in the grid
     */
    private static void printToImage(String[][] bingo, String pathToMiddleImage) throws IOException {
        int cellSize = 200;
        int padding = 50;
        int headerHeight = 200;
        // total width of the grid
        int width = bingo[0].length * cellSize + 2*padding;
        int height = bingo.length * cellSize + 2*padding + headerHeight;
        int headerFontSize = 150;

        // create a colour image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Graphics2D object to draw on the image
        Graphics2D g2d = image.createGraphics();

        // Fill background in white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Draw header
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, headerFontSize));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        String headerText = "BINGO";
        int headerWidth = fontMetrics.stringWidth(headerText);
        g2d.drawString(headerText, (width - headerWidth) / 2, padding+headerFontSize);

        // Draw grid lines
        g2d.setColor(Color.BLACK);
        // Horizontal lines
        for (int i = 0; i <= bingo.length; i++) {
            g2d.drawLine(padding, headerHeight + padding + (i * cellSize),
                    width - padding, headerHeight + padding + (i * cellSize));
        }
        // Vertical lines
        for (int j = 0; j <= bingo[0].length; j++) {
            g2d.drawLine(padding + (j * cellSize), headerHeight + padding,
                    padding + (j * cellSize), height - padding);
        }

        // Draw text in cells
        int cellFontSize = 30;
        g2d.setFont(new Font("Arial", Font.PLAIN, cellFontSize));
        FontMetrics cellFontMetrics = g2d.getFontMetrics();
        for (int i = 0; i < bingo.length; i++) {
            for (int j = 0; j < bingo[i].length; j++) {
                String cellText = bingo[i][j];
                // Wrap the text to not exceed the cell width
                List<String> lines = wrapText(cellText, cellFontMetrics, cellSize-cellFontSize);

                // Calculate the Y position for text in this cell
                int startY = headerHeight + padding + (i*cellSize) +
                        (cellSize - (lines.size() * cellFontSize)) / 2 +
                        cellFontMetrics.getAscent();

                // Draw each line of text
                for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                    String line = lines.get(lineIndex);
                    // Calculate the X position for each line of text
                    int lineWidth = cellFontMetrics.stringWidth(line);
                    int startX = padding + j*cellSize + (cellSize-lineWidth)/2;

                    // Draw the line
                    g2d.drawString(line, startX, startY + (lineIndex * cellFontSize));
                }
            }
        }

        // Insert image in the middle
        // Get original image
        BufferedImage imageMiddleCell = ImageIO.read(new File(pathToMiddleImage));
        // Scale image to fit within cell
        BufferedImage resizedImageMiddleCell = resizeImage(imageMiddleCell, cellSize);
        // Calculate position of image
        int middleImageY = headerHeight + padding + 2*cellSize + (cellSize- resizedImageMiddleCell.getHeight())/2;
        int middleImageX = padding + 2*cellSize + (cellSize-resizedImageMiddleCell.getWidth())/2;
        g2d.drawImage(resizedImageMiddleCell, middleImageX, middleImageY, null);

        // Dispose of the Graphics2D object
        g2d.dispose();

        try {
            File outputFile = new File(System.getProperty("user.home") + "/Desktop/poirot_bingo.png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved to desktop as 'poirot_bingo.png'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * changes the size of baseImage to ensure it fits within the cell
     * @param baseImage original image
     * @param cellSize dimensions of cell that image must fit it
     * @return newly scaled image
     */
    private static BufferedImage resizeImage(BufferedImage baseImage, int cellSize) {
        int originalWidth = baseImage.getWidth();
        int originalHeight = baseImage.getHeight();

        // Calculate scaling factor
        double scaleFactor = Math.min((double) cellSize / originalWidth, (double) cellSize / originalHeight) * 0.99;

        // Calculate new dimensions
        int newWidth = (int) (originalWidth * scaleFactor);
        int newHeight = (int) (originalHeight * scaleFactor);

        // Create new BufferedImage with adjusted size
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        // Draw the scaled image
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(baseImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * wraps text to fit within a specified width
     * @param text strings to wrap
     * @param fm font metrics of the text
     * @param maxWidth maximum width that text can have
     * @return text split into sections that do not exceed the maxWidth
     */
    private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");  // Split the text into words
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Check if adding this word would exceed the maximum width
            if (fm.stringWidth(currentLine + " " + word) <= maxWidth) {
                // If not, add it to the current line
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            } else {
                // If it exceeded the width, start a new line
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                // If a single word is wider than maxWidth, it will be on its own line
                currentLine.append(word);
            }
        }

        // Add the last line if it's not empty
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    public static String[] poirotContent = {
            "Poirot mangles an English saying",
            "Poirot gives his business card",
            "Casual racism",
            "Mention of Hitler or the war",
            "Poirot uses his pince-nez glasses",
            "Train appears in the frame",
            "Poirot rearranges things",
            "Someone calls Poirot 'French'",
            "Poirot: 'mon ami'",
            "Poirot manicures his moustache",
            "Poirot: 'mademoiselle'",
            "Ms. Lemon researches information",
            "Poirot subtly insults someone of lesser intellect",
            "Poirot: 'I am Belgian'",
            "Inspector Japp has a wrong conclusion",
            "Police chase",
            "Poirot is insulted!",
            "Hastings obsesses about cars",
            "Sexism",
            "A woman screams",
            "Suspects are gathered for 'summing up'",
            "Poirot: 'little grey cells'",
            "Poirot: 'a matter of life and death",
            "Poirot is furious about himself",
            "Poirot drinks tea",
            "Hastings: 'I say!'",
            "Hastings: 'Good Lord!'",
            "Victim is poisoned",
            "Hastings asks around for information",
            "Someone wears a disguise",
            "Ms. Lemon wears a stylish hat",
            "Poirot talks about himself in 3rd person",
            "Married couple hates each other",
            "Classism",
            "Poirot walks with his cane",
            "Poirot dislikes food or a performance",
            "Someone reads the newspaper",
            "Inspector Japp says sth. funny",
            "The white house",
            "Poirot's office building from the outside",
            "Someone talks on the phone",
            "Someone is observed",
            "Taxi"
    };

}