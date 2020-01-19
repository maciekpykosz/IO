package model.dependency;

public class Difference {
    private String modifier;
    private String fileName;

    public Difference(String modifier, String fileName) {
        this.modifier = modifier;
        this.fileName = fileName;
    }

    public String getModifier() {
        return modifier;
    }

    public String getFileName() {
        return fileName;
    }
}
