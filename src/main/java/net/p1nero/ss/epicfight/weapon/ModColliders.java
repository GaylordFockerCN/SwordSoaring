package net.p1nero.ss.epicfight.weapon;

import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;

public class ModColliders {
    public static final Collider LOONG_ROAR = new MultiOBBCollider(6, 0.3, 0.3, 1.4, 0.0, 0.0, -0.8D);

    public static final Collider LOONG_ROAR_RANGE = new OBBCollider(2.0, 2.0, 2.0, 0.0, 0.0, 0.0);

}
