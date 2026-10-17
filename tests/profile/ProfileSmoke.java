package me.eldodebug.soar.management.profile;

import me.eldodebug.soar.management.language.TranslateText;
import com.google.gson.*;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.*;
import me.eldodebug.soar.management.event.EventManager;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.*;
import me.eldodebug.soar.management.mods.*;
import me.eldodebug.soar.management.mods.impl.GlobalSettingsMod;
import me.eldodebug.soar.management.mods.settings.impl.*;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import net.minecraft.client.Minecraft;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

public final class ProfileSmoke {
    private static final Unsafe UNSAFE;
    static { try { Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);UNSAFE=(Unsafe)f.get(null); }catch(Exception e){throw new ExceptionInInitializerError(e);} }
    private static void set(Class<?> type,Object owner,String name,Object value)throws Exception {Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(owner,value);}
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    private static Queue<FutureTask<?>> tasks=new ArrayDeque<FutureTask<?>>();
    private static void drain(){while(true){FutureTask<?> task;synchronized(tasks){task=tasks.poll();}if(task==null)return;task.run();}}
    private static void await(BooleanSupplier condition)throws Exception {long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(7);while(System.nanoTime()<deadline){drain();if(condition.getAsBoolean())return;Thread.sleep(10);}throw new AssertionError("Profile operation deadline");}
    private static final class TestHud extends HUDMod {
        final BooleanSetting flag=new BooleanSetting(TranslateText.MODE,this,true);
        final NumberSetting number=new NumberSetting(TranslateText.SCALE,this,1,0,5,false);
        TestHud(){super(TranslateText.FPS_DISPLAY,TranslateText.FPS_DISPLAY_DESCRIPTION);}
    }
    public static void main(String[] args)throws Exception {
        Path root=Files.createTempDirectory("soar-profile-qa-");
        Minecraft mc=(Minecraft)UNSAFE.allocateInstance(Minecraft.class);set(Minecraft.class,null,"theMinecraft",mc);
        set(Minecraft.class,mc,"mcDataDir",root.toFile());set(Minecraft.class,mc,"scheduledTasks",tasks);set(Minecraft.class,mc,"mcThread",Thread.currentThread());
        Soar soar=Soar.getInstance();set(Soar.class,soar,"mc",mc);
        set(Soar.class,soar,"fileManager",new FileManager());set(Soar.class,soar,"languageManager",new LanguageManager());set(Soar.class,soar,"eventManager",new EventManager());
        ModManager mods=new ModManager();set(Soar.class,soar,"modManager",mods);mods.getMods().add(new GlobalSettingsMod());TestHud hud=new TestHud();mods.getMods().add(hud);
        ColorManager colors=new ColorManager();set(Soar.class,soar,"colorManager",colors);
        colors.setTheme(Theme.DARK);soar.getLanguageManager().setCurrentLanguage(Language.CHINESE);
        ProfileCodec codec=new ProfileCodec(new BackgroundManager());JsonObject legacy=codec.snapshot("",ProfileType.ALL,ProfileIcon.GRASS);
        Path directory=soar.getFileManager().getProfileDir().toPath();ProfileFiles.write(directory.resolve("Default.json"),legacy);
        colors.setTheme(Theme.LIGHT);soar.getLanguageManager().setCurrentLanguage(Language.ENGLISH);
        ProfileManager manager=new ProfileManager();set(Soar.class,soar,"profileManager",manager);
        check(manager.getCurrent()!=null&&manager.getCurrent().isDefault(),"Default missing after migration");
        check(colors.getTheme()==Theme.DARK&&soar.getLanguageManager().getCurrentLanguage()==Language.CHINESE,"Legacy global preferences not migrated");
        hud.setToggled(true);manager.create("Bedwars");await(()->!manager.isWorking()&&manager.getCurrent().getName().equals("Bedwars"));Profile bedwars=manager.getCurrent();
        hud.flag.setToggled(false);hud.number.setValue(2);hud.setX(210);hud.setScale(1.3f);Thread.sleep(520);manager.tick();await(()->manager.getStatus().equals(TranslateText.PROFILE_SAVED.getText()));
        JsonObject saved=ProfileFiles.read(bedwars.getJsonFile().toPath());check(saved.getAsJsonObject("Mods").getAsJsonObject(hud.getNameKey()).get("X").getAsInt()==210,"HUD not autosaved");
        Profile defaults=manager.getProfiles().stream().filter(Profile::isDefault).findFirst().get();manager.use(defaults);await(()->!manager.isWorking()&&manager.getCurrent()==defaults);
        check(hud.flag.isToggled()&&hud.getX()==100,"Switch retained previous profile state");check(colors.getTheme()==Theme.DARK,"Profile changed global theme");
        String before=ProfileFiles.hash(ProfileFiles.read(bedwars.getJsonFile().toPath()).get("Mods"));manager.updateMetadata(bedwars,ProfileType.FAVORITE,ProfileIcon.TNT,"hypixel.net");await(()->!manager.isWorking());
        check(before.equals(ProfileFiles.hash(ProfileFiles.read(bedwars.getJsonFile().toPath()).get("Mods"))),"Favoriting overwrote inactive module data");
        manager.rename(bedwars,"Bridge");manager.use(bedwars);await(()->!manager.isWorking()&&manager.getCurrent()==bedwars);check(!hud.flag.isToggled()&&hud.getX()==210,"Saved profile did not restore");
        manager.delete(defaults);manager.rename(defaults,"Wrong");check(defaults.getName().equals("Default")&&defaults.getJsonFile().exists(),"Default protection failed");
        int files=manager.getProfiles().size();manager.importFile(bedwars.getJsonFile());await(()->!manager.isWorking()&&manager.getProfiles().size()==files+1);check(manager.getCurrent()==bedwars,"Import unexpectedly applied");
        Path preferences=directory.getParent().resolve("client-settings.json"),held=preferences.resolveSibling("held-preferences.json");
        Files.move(preferences,held);Files.createDirectory(preferences);Files.write(preferences.resolve("blocked"),new byte[]{1});manager.use(defaults);await(()->!manager.isWorking());check(manager.getCurrent()==bedwars&&manager.isFailed(),"Write failure switched away or claimed success");
        Files.delete(preferences.resolve("blocked"));Files.delete(preferences);Files.move(held,preferences);
        manager.shutdown();set(Soar.class,soar,"profileManager",null);ProfileManager restarted=new ProfileManager();set(Soar.class,soar,"profileManager",restarted);
        check(restarted.getCurrent().getName().equals("Bridge"),"Active profile not restored after restart");check(hud.getX()==210&&!hud.flag.isToggled(),"Autosave did not survive restart");
        restarted.shutdown();check(Files.isDirectory(directory.getParent().resolve("backups")),"Migration backup missing");
        Path index=directory.getParent().resolve("profiles-index.json");JsonObject corruptIndex=new JsonObject();corruptIndex.addProperty("profiles",42);ProfileFiles.write(index,corruptIndex);
        set(Soar.class,soar,"profileManager",null);ProfileManager recovered=new ProfileManager();set(Soar.class,soar,"profileManager",recovered);
        check(recovered.getCurrent()!=null&&recovered.getProfiles().size()==files+1,"Malformed index lost native profiles");recovered.shutdown();
        JsonObject mixed=ProfileFiles.read(index);mixed.getAsJsonObject("profiles").addProperty(bedwars.getJsonFile().getName(),false);ProfileFiles.write(index,mixed);
        set(Soar.class,soar,"profileManager",null);recovered=new ProfileManager();set(Soar.class,soar,"profileManager",recovered);
        check(recovered.getProfiles().size()==files+1,"Malformed index record hid a valid native profile");recovered.shutdown();
        boolean duplicate=false;try{ProfileFiles.parse("{\"a\":1,\"a\":2}");}catch(java.io.IOException expected){duplicate=true;}check(duplicate,"Duplicate keys accepted");
        System.out.println("PASS profiles: migration, independent autosave, safe switching, global preferences, metadata, import, Default protection, disk failure and restart");
    }
}
