package it.unisa.theneverendingrun.obstaclesManager;

import com.badlogic.gdx.Gdx;
import it.unisa.theneverendingrun.models.obstacles.*;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class ObstaclesManager {

    //TODO: Togliere
    static final int OFFSET = (int) (0.0625 * Gdx.graphics.getHeight());
    static final float MULTIPLIER = 5;

    /**
     * Values which are needed to set the correct position of the new obstacle.
     */
    private float maxJumpingHeight;
    private float standingHeight;
    private float slidingHeight;
    private float standingWidth;

    /**
     * Reference to the obstacleFactory.
     */
    private static ObstacleFactory obstacleFactory;

    /**
     * Reference to the last obstacle generated.
     */
    private AbstractObstacle lastObstacle;

    /**
     * Constructor of the obstaclesManager. parameters are self explanatory
     * todo update and complete this javadoc
     */
    public ObstaclesManager(float maxJumpingHeight, float standingHeight, float maxSlidingDistance, float slidingHeight, float standingWidth) {
        this.maxJumpingHeight = maxJumpingHeight;
        this.standingHeight = standingHeight;
        this.slidingHeight = slidingHeight;
        this.standingWidth = standingWidth;
        obstacleFactory = new ObstacleFactory(maxJumpingHeight, maxSlidingDistance, standingWidth * MULTIPLIER);//fixme
    }

    /**
     * This method will randomly create and return a new obstacle. The obstacles are generated by following some
     * criteria, ensuring that the character can avoid it.
     * In addition, to the obstacle will be assigned the correct position, based on the reference measures given
     * during the creation of the obstaclesManager.
     *
     * @return A new AbstractObstacle, with the correct position, null if the obstacle cannot be generated
     */
    public AbstractObstacle generateNewObstacle() {
        ObstacleType newObstacleType = getAppropriateObstacleType();
        if (newObstacleType == null) {
            return null;
        }
        AbstractObstacle newObstacle = obstacleFactory.getObstacle(newObstacleType, 0, 0);
        setPosition(newObstacle);
        lastObstacle = newObstacle;
        return newObstacle;
    }

    /**
     * This method is used to get the right type of obstacle that can be added to the path, following the conditions.
     * For example, if the last obstacle was a slidable one, we cannot put another right after it,
     * otherwise the player might not be able to pass.
     * Please, note that this method randomly decides to add or not an obstacle, even if it can added.
     *
     * @return The type of obstacle that can be added, null if none. //fixme maybe raise an exception?
     */
    private ObstacleType getAppropriateObstacleType() {
        //If there isn't any obstacle on the screen, add one at random
        if (lastObstacle == null) {
            int random = ThreadLocalRandom.current().nextInt(ObstacleType.values().length);
            return ObstacleType.values()[random];
        }

        // Calculate the distance from the last obstacle. This distance is defined as the distance from the right
        // side of an obstacle to the left side of the view.
        int distance = (int) (Gdx.graphics.getWidth() - lastObstacle.getX() - lastObstacle.getWidth());

        // If distance is less than zero, the obstacle is still not completely visible, so wait
        if (distance < 0)
            return null;

        // If distance is zero, then we could add a jumpable obstacle, but only if the previous was of this type
        if (distance == 0) {
            if (lastObstacle instanceof JumpableObstacle) {
                //fixme tune the probability
                if (ThreadLocalRandom.current().nextBoolean())
                    return ObstacleType.Slidable;
                if (ThreadLocalRandom.current().nextBoolean())
                    return ObstacleType.Jumpable;

            }
            if (lastObstacle instanceof JumpableSlidableObstacle) {
                return null;
            }
            if (lastObstacle instanceof SlidableObstacle) {
                return null;
            }
        }

        //If the space is not sufficient for the hero to pass, wait.
        if (distance < standingWidth * MULTIPLIER) {//fixme tune the distance
            return null;
        }

        return ObstacleType.Slidable;/*
        // If the obstacle is distant enough, it is possible to add every type of obstacle
        if (distance >= standingWidth * MULTIPLIER) {//fixme tune the probability and the distance
            if (ThreadLocalRandom.current().nextInt() % 20 == 0) {
                int random = ThreadLocalRandom.current().nextInt(0, ObstacleType.values().length);
                return ObstacleType.values()[random];
            }
        }
        return null;*/
    }

    /**
     * This method will fix the position of the given obstacle. This will take into account the dimensions of the
     * obstacles, allowing to vary the position, keeping it avoidable by the user. If the position was already fixed,
     * it will change randomly, but always in a range that allows to avoid it.
     * The position on the x axis is always at the rightmost edge, while on the y-axis depends on the dimension of the
     * obstacle and on the parameters passed to the constructor.
     *
     * @param obstacle the obstacle which needs the position fixed
     */
    private void setPosition(AbstractObstacle obstacle) {
        int yPosition = 0;
        if (obstacle instanceof JumpableObstacle) {
            yPosition = 0;
        } else if (obstacle instanceof SlidableObstacle) {
            yPosition = ThreadLocalRandom.current().nextInt((int) slidingHeight + 2, (int) standingHeight - 1);
            if (lastObstacle != null) {
                if (lastObstacle instanceof JumpableObstacle && lastObstacle.getX() + lastObstacle.getWidth() >= Gdx.graphics.getWidth() - 1) {
                    yPosition += lastObstacle.getHeight() + lastObstacle.getY();
                }
            }
        } else if (obstacle instanceof JumpableSlidableObstacle) {
            yPosition = ThreadLocalRandom.current().nextInt((int) slidingHeight + 1,
                    1 + (int) slidingHeight + (int) maxJumpingHeight - (int) obstacle.getHeight());
        }
        // Accounting for the lower part of the background
        yPosition += OFFSET;
        obstacle.setPosition(Gdx.graphics.getWidth(), yPosition);
    }

    /**
     * This method will remove from memory the obstacles which are not visible anymore.
     *
     * @param obstacles the LinkedList which contains all the obstacles.
     */
    public void clearOldObstacles(LinkedList<AbstractObstacle> obstacles) {
        if (obstacles.isEmpty())
            return;

        LinkedList<AbstractObstacle> toRemoveList = new LinkedList<>();
        for (AbstractObstacle obstacle : obstacles)
            if (!obstacle.isXAxisVisible()) {
                toRemoveList.add(obstacle);
            }
        for (AbstractObstacle toRemove : toRemoveList) {
            obstacles.remove(toRemove);
        }
    }


    @Deprecated
    public void updateObstaclesPosition(LinkedList<AbstractObstacle> obstacles) {
        for (AbstractObstacle obs : obstacles
        ) {
            obs.setX(obs.getX() - 8);
        }
    }
}
