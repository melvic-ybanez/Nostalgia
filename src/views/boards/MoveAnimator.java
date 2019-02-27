package views.boards;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

/**
 * Created by melvic on 2/9/19.
 *
 */
public abstract class MoveAnimator {
    private BoardView boardView;

    public MoveAnimator(BoardView boardView) {
        this.boardView = boardView;
    }

    public void animate(GraphicsContext gc, int sourceFile, int sourceRank, int destFile, int destRank) {
        double sourceX = sourceFile * boardView.squareSize();
        double sourceY = sourceRank * boardView.squareSize();

        double destX = destFile * boardView.squareSize();
        double destY = destRank * boardView.squareSize();

        DoubleProperty x = new SimpleDoubleProperty();
        DoubleProperty y = new SimpleDoubleProperty();

        double timeLimit = 0.3;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(x, sourceX),
                        new KeyValue(y, sourceY)),
                new KeyFrame(Duration.seconds(timeLimit),
                        new KeyValue(x, destX),
                        new KeyValue(y, destY))
        );

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                MoveAnimator.this.handle(now, x, y);
                if (x.doubleValue() == destX && y.doubleValue() == destY) {
                    updateGameState();
                    this.stop();
                }
            }
        };
        timer.start();
        timeline.play();
    }

    public abstract void handle(long now, DoubleProperty x, DoubleProperty y);

    public abstract void updateGameState();
}
