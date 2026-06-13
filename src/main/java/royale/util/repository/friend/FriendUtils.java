package royale.util.repository.friend;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import royale.util.config.impl.friend.FriendConfig;
public final class FriendUtils
{
private FriendUtils() {
throw new UnsupportedOperationException("This is a utility class and cannot be instantiated"); } public static List<Friend> getFriends() {
return friends;
} private static final List<Friend> friends = new ArrayList<>();
public static void addFriend(PlayerEntity player) {
addFriend(player.getName().getString());
}
public static void addFriend(String name) {
if (!isFriend(name)) {
friends.add(new Friend(name));
}
}
public static void addFriendAndSave(String name) {
addFriend(name);
FriendConfig.getInstance().save();
}
public static void removeFriend(PlayerEntity player) {
removeFriend(player.getName().getString());
}
public static void removeFriend(String name) {
friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
}
public static void removeFriendAndSave(String name) {
removeFriend(name);
FriendConfig.getInstance().save();
}
public static boolean isFriend(Entity entity) {
if (entity instanceof PlayerEntity) { PlayerEntity player = (PlayerEntity)entity;
return isFriend(player.getName().getString()); }
return false;
}
public static boolean isFriend(String friend) {
return friends.stream().anyMatch(isFriend -> isFriend.getName().equalsIgnoreCase(friend));
}
public static void clear() {
friends.clear();
}
public static void clearAndSave() {
clear();
FriendConfig.getInstance().save();
}
public static List<String> getFriendNames() {
return (List<String>)friends.stream().map(Friend::getName).collect(Collectors.toList());
}
public static int size() {
return friends.size();
}
public static void setFriends(List<String> names) {
friends.clear();
for (String name : names)
friends.add(new Friend(name)); 
}
}


