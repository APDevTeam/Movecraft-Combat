package net.countercraft.movecraft.combat.movecraftcombat.tracking;

public enum DamageType {
    CANNON {
        public String toString() {
            return "Cannon";
        }
    },
    FIREBALL {
        public String toString() {
            return "Fireball";
        }
    },
    TORPEDO {
        public String toString() {
            return "Torpedo";
        }
    };
}

