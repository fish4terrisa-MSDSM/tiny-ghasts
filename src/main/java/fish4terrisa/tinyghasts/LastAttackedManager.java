package fish4terrisa.tinyghasts;
import net.minecraft.entity.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LastAttackedManager {
    public static final Map<UUID, Entity> lastAttacked = new HashMap<>();
}
