package it.unicam.cs.bdslab.extrarnas.view.utils;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;

import java.nio.file.Path;

public class LenCell extends TableCell<Path, Path> {

    private final ImageButton imageButton;

    public LenCell(Image image) {
        this.imageButton = new ImageButton(image);
    }

    @Override
    protected void updateItem(Path rnaFile, boolean empty) {
        super.updateItem(rnaFile, empty);
        if (rnaFile == null) {
            setGraphic(null);
            return;
        }
        setGraphic(imageButton);

    }

}
