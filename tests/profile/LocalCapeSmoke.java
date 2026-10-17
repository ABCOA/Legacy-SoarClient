import me.eldodebug.soar.management.cape.*;
import me.eldodebug.soar.management.cape.impl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;

public final class LocalCapeSmoke {
    private static void set(Class<?> type,Object owner,String name,Object value)throws Exception {
        Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(owner,value);
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe unsafe=(Unsafe)f.get(null);
        Minecraft mc=(Minecraft)unsafe.allocateInstance(Minecraft.class);
        UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        set(Minecraft.class,mc,"session",new Session("PlayerA",a.toString(),"unused","mojang"));
        CapeManager manager=(CapeManager)unsafe.allocateInstance(CapeManager.class);
        ArrayList<Cape> capes=new ArrayList<>();
        Cape official=new OfficialCape(),none=new NormalCape("None",null,null,false,CapeCategory.ALL);
        capes.add(official);capes.add(none);
        java.io.File file=Files.createTempDirectory("local-cape-test-").resolve("selection.properties").toFile();
        CapeSelectionStore store=new CapeSelectionStore(file);
        ThreadPoolExecutor worker=(ThreadPoolExecutor)Executors.newFixedThreadPool(1);
        set(CapeManager.class,manager,"mc",mc);set(CapeManager.class,manager,"capes",capes);
        set(CapeManager.class,manager,"store",store);set(CapeManager.class,manager,"workers",worker);
        try {
            check(manager.getCurrentCape()==official,"Initial account must use official cape");
            manager.setCurrentCape(none);
            check(manager.getCurrentCape()==none,"Local selection did not change");
            check(store.getSelection(a).equals("none"),"Selection persisted against wrong account");
            set(Minecraft.class,mc,"session",new Session("PlayerB",b.toString(),"unused","mojang"));
            check(manager.getCurrentCape()==official,"Account change reused old selection");
            set(Minecraft.class,mc,"session",new Session("PlayerA",a.toString(),"unused","mojang"));
            check(manager.getCurrentCape()==none,"Returning account lost its selection");
            store.save();check(new CapeSelectionStore(file).getSelection(a).equals("none"),"Local selection did not survive reload");
            System.out.println("PASS local capes: selection, account isolation and persistence without service integration");
        } finally {manager.shutdown();worker.awaitTermination(3,TimeUnit.SECONDS);}
    }
}
