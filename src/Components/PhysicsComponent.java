package Components;

import Simulation.Entity;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import Environment.Locator;
import Environment.Physics;

/**
 *
 * @author rob
 */
public class PhysicsComponent{
    private final Vector3f spatialScale;
    private final Entity entity;
    private final Spatial spatial;
    private final BetterCharacterControl spatialControl;
    private Vector3f position;
    private SimpleApplication app;
    private GhostControl proximityBox;
    private Physics physics = Locator.getPhysics();
    
    private Random randMass;

    private static float DIRECTION_LENGTH = 20;

    public PhysicsComponent(Entity entity) {
        this.app = app;
        this.entity = entity;
        this.spatial = entity.getSpatial();
        this.spatialScale = spatial.getLocalTransform().getScale();
        this.position = spatial.getLocalTranslation();

        /*
            the phsysic body has a random mass, this is to prevent
            entities getting stuck while in collision with another entity.
            The heaviest body wins the collision.
        */
        randMass = new Random();
        spatialControl = new BetterCharacterControl(1f, 9f, (randMass.nextInt(10) + 1));
        
        setControlEnabled(true);
    }

    public void setControlEnabled(final boolean value) {
        boolean hasControl = spatial.getControl(BetterCharacterControl.class) != null;
        boolean spaceNotEmpty = physics.getRBodies().size() != 0;

        if (value) {
            spatial.addControl(spatialControl);
            spatialControl.setGravity(new Vector3f(0, -40, 0));
            spatialControl.setJumpForce(new Vector3f(0, 1, 0));
            physics.addToSpace(spatialControl);
            physics.addToSpace(spatial);
        } else if (hasControl && spaceNotEmpty) {
            spatial.removeControl(spatialControl);         
            physics.removeFromSpace(spatialControl);
            physics.removeFromSpace(spatial);
        }
    }

    public BetterCharacterControl getControl() {
        return spatialControl;
    }

    public void initProximityBox(final float size) {
        var boxSize = new Vector3f(size, size, size);
        var boxCollShape = new BoxCollisionShape(boxSize);

        proximityBox = new GhostControl(boxCollShape);

        spatial.addControl(proximityBox);
        proximityBox.setUserObject(entity);
        physics.addToSpace(proximityBox);
    }

    public Set<Entity> getNearEntities() {
        int nNear = proximityBox.getOverlappingCount();

        if (nNear != 0) {
            var nearEntities = proximityBox.getOverlappingObjects()
                    .stream()
                    //.filter(o -> (o.getUserObject() instanceof Entity))
                    .filter(o -> o instanceof GhostControl)
                    .map(o -> (Entity) o.getUserObject())
                    .collect(Collectors.toSet());

            return nearEntities;
        }

        return Collections.EMPTY_SET;
    }

    public Optional<Float> getNearDistance(final Entity guest) {

        boolean isNear = getNearEntities().contains(guest);
        var guestPos = guest.getPosition();

        if (isNear) {
            float distance = position.distance(guestPos);

            return Optional.of(distance);
        }

        return Optional.empty();
    }

    public void update() {
        position = spatial.getLocalTranslation();
    }
}
