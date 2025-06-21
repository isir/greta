package greta.auxiliary.emotion.model;

import java.time.Instant;

/**
 * Facial input data for facial emotion analysis
 */
public class FaceInput {
    
    private byte[] imageData;
    private ImageFormat format;
    private int width;
    private int height;
    private int channels;
    private Instant timestamp;
    private String sourceId;
    private FaceRegion faceRegion;
    
    public FaceInput() {
        this.timestamp = Instant.now();
    }
    
    public FaceInput(byte[] imageData, int width, int height) {
        this();
        this.imageData = imageData;
        this.width = width;
        this.height = height;
    }
    
    public enum ImageFormat {
        JPEG,
        PNG,
        BMP,
        TIFF,
        RAW_RGB,
        RAW_BGR,
        RAW_GRAYSCALE
    }
    
    /**
     * Face region coordinates in the image
     */
    public static class FaceRegion {
        private int x;
        private int y;
        private int width;
        private int height;
        private double confidence;
        
        public FaceRegion(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        // Getters and setters
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
    
    // Getters and setters
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    
    public ImageFormat getFormat() { return format; }
    public void setFormat(ImageFormat format) { this.format = format; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public int getChannels() { return channels; }
    public void setChannels(int channels) { this.channels = channels; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    
    public FaceRegion getFaceRegion() { return faceRegion; }
    public void setFaceRegion(FaceRegion faceRegion) { this.faceRegion = faceRegion; }
}