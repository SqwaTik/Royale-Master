package royale.util.repository.staff;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import royale.util.config.impl.staff.StaffConfig;
public final class StaffUtils
{
private StaffUtils() {
throw new UnsupportedOperationException("This is a utility class and cannot be instantiated"); } public static List<Staff> getStaffList() {
return staffList;
} private static final List<Staff> staffList = new ArrayList<>();
public static void addStaff(String name) {
if (!isStaff(name)) {
staffList.add(new Staff(name));
}
}
public static void addStaffAndSave(String name) {
addStaff(name);
StaffConfig.getInstance().save();
}
public static void removeStaff(String name) {
staffList.removeIf(staff -> staff.getName().equalsIgnoreCase(name));
}
public static void removeStaffAndSave(String name) {
removeStaff(name);
StaffConfig.getInstance().save();
}
public static boolean isStaff(Entity entity) {
if (entity instanceof PlayerEntity) { PlayerEntity player = (PlayerEntity)entity;
return isStaff(player.getName().getString()); }
return false;
}
public static boolean isStaff(String name) {
return staffList.stream().anyMatch(staff -> staff.getName().equalsIgnoreCase(name));
}
public static void clear() {
staffList.clear();
}
public static void clearAndSave() {
clear();
StaffConfig.getInstance().save();
}
public static List<String> getStaffNames() {
return (List<String>)staffList.stream().map(Staff::getName).collect(Collectors.toList());
}
public static int size() {
return staffList.size();
}
public static void setStaff(List<String> names) {
staffList.clear();
for (String name : names)
staffList.add(new Staff(name)); 
}
}


