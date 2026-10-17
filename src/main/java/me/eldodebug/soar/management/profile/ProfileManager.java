package me.eldodebug.soar.management.profile;

import me.eldodebug.soar.management.language.TranslateText;
import com.google.gson.*;
import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ProfileManager {
    private final CopyOnWriteArrayList<Profile> profiles = new CopyOnWriteArrayList<Profile>();
    private final BackgroundManager backgroundManager = new BackgroundManager();
    private final ProfileCodec codec = new ProfileCodec(backgroundManager);
    private final Path directory = Soar.getInstance().getFileManager().getProfileDir().toPath();
    private final Path indexFile = directory.getParent().resolve("profiles-index.json");
    private final Path preferencesFile = directory.getParent().resolve("client-settings.json");
    private final ExecutorService writer = Executors.newSingleThreadExecutor(task -> { Thread t=new Thread(task,"Soar Profile Writer");t.setDaemon(true);return t; });
    private final JsonObject factory;
    private Profile current;
    private volatile boolean working, writing, failed, stopped;
    private boolean loading=true;
    private long revision, writtenRevision, firstDirty, lastDirty, nextPreferencesCheck;
    private String persistedPreferences="", migrationNotice="";
    private int nextId;
    private interface Disk<T> { T run() throws Exception; }

    public ProfileManager() {
        factory=codec.snapshot("",ProfileType.ALL,ProfileIcon.GRASS);
        try {
            Files.createDirectories(directory);
            boolean migrating=!Files.exists(indexFile);
            if(migrating) {
                Path backup=directory.getParent().resolve("backups").resolve("profiles-migration-"+UUID.randomUUID());
                Files.createDirectories(backup);
                try(DirectoryStream<Path> files=Files.newDirectoryStream(directory,"*.json")) {
                    for(Path file:files) Files.copy(file,backup.resolve(file.getFileName()));
                }
            }
            JsonObject index=new JsonObject();
            if(Files.exists(indexFile))try{index=ProfileFiles.recovered(indexFile);}catch(IOException invalid){quarantine(indexFile);migrationNotice=TranslateText.PROFILE_RECOVERED.getText();}
            if(index.has("profiles")&&!index.get("profiles").isJsonObject()){quarantine(indexFile);index=new JsonObject();migrationNotice=TranslateText.PROFILE_RECOVERED.getText();}
            JsonObject records=index.has("profiles")?index.getAsJsonObject("profiles"):new JsonObject();
            try(DirectoryStream<Path> files=Files.newDirectoryStream(directory,"*.json")) {
                for(Path file:files) {
                    try {
                        JsonObject nativeJson=readProfile(file);
                        JsonElement stored=records.get(file.getFileName().toString());
                        JsonObject record=stored!=null&&stored.isJsonObject()?stored.getAsJsonObject():new JsonObject();
                        if(stored!=null&&!stored.isJsonObject())migrationNotice=TranslateText.PROFILE_RECOVERED.getText();
                        Profile p=new Profile(nextId++,str(nativeJson.getAsJsonObject("Profile Data"),"Server",""),file.toFile(),ProfileIcon.getIconById(integer(nativeJson.getAsJsonObject("Profile Data"),"Icon",3)));
                        p.setStableId(str(record,"id",UUID.randomUUID().toString()));
                        try{UUID.fromString(p.getStableId());}catch(RuntimeException invalid){p.setStableId(UUID.randomUUID().toString());}
                        p.setName(p.isDefault()?"Default":str(record,"name",p.getName()));
                        p.setType(ProfileType.getTypeById(integer(nativeJson.getAsJsonObject("Profile Data"),"Type",0)));
                        p.setSource(str(record,"sourceId",""),str(record,"sourceHash",""),str(record,"importedStateHash",""));
                        profiles.add(p);
                    } catch(Exception invalid){migrationNotice=TranslateText.PROFILE_RECOVERED.getText();SoarLogger.warn("Skipped an invalid local profile");}
                }
            }
            Profile defaults=defaultProfile();
            if(defaults==null) {
                Path file=directory.resolve("Default.json");
                if(Files.exists(file))Files.move(file,file.resolveSibling("Default.invalid-"+UUID.randomUUID()));
                ProfileFiles.write(file,factory);
                defaults=new Profile(nextId++,"",file.toFile(),ProfileIcon.GRASS);defaults.setStableId(UUID.randomUUID().toString());profiles.add(0,defaults);
            }
            try{
                if(Files.exists(preferencesFile))codec.applyPreferences(ProfileFiles.recovered(preferencesFile));
                else {codec.applyPreferences(readProfile(defaults.getJsonFile().toPath()));ProfileFiles.write(preferencesFile,codec.preferences());}
            }catch(Exception invalid){if(Files.exists(preferencesFile))quarantine(preferencesFile);codec.applyPreferences(readProfile(defaults.getJsonFile().toPath()));ProfileFiles.write(preferencesFile,codec.preferences());migrationNotice=TranslateText.PROFILE_RECOVERED.getText();}
            persistedPreferences=codec.preferences().toString();
            String active=str(index,"active",defaults.getStableId());
            for(Profile p:profiles)if(p.getStableId().equals(active))current=p;
            if(current==null)current=defaults;
            codec.apply(ProfileCodec.prepare(factory,readProfile(current.getJsonFile().toPath())));
            ProfileFiles.write(indexFile,index(current,profiles));
            if(migrating&&migrationNotice.isEmpty())migrationNotice=TranslateText.PROFILE_MIGRATED.getText();
        }catch(Exception failure){
            failed=true;migrationNotice=TranslateText.PROFILE_RECOVERED.getText();SoarLogger.error("Profile initialization failed; original files were preserved",failure);
        }finally{loading=false;}
    }
    public static void changed() {
        Soar soar=Soar.getInstance();
        if(soar!=null&&soar.getProfileManager()!=null)soar.getProfileManager().markDirty();
    }
    public static void changed(Setting setting) { if(!ProfileCodec.global(setting))changed(); }
    public void markDirty() {
        if(loading||stopped||current==null)return;
        long now=System.nanoTime();if(revision==writtenRevision)firstDirty=now;lastDirty=now;revision++;
    }
    public void tick() {
        if(stopped||loading||working||current==null)return;
        long now=System.nanoTime();
        if(!writing&&revision!=writtenRevision&&(now-lastDirty>=TimeUnit.MILLISECONDS.toNanos(500)||now-firstDirty>=TimeUnit.SECONDS.toNanos(2)))save();
        if(now>=nextPreferencesCheck){nextPreferencesCheck=now+TimeUnit.MILLISECONDS.toNanos(500);JsonObject prefs=codec.preferences();if(!writing&&!prefs.toString().equals(persistedPreferences))save();}
    }
    public void save() { if(!working&&!writing)flush(null); }
    public void flush(Consumer<Boolean> callback) {
        if(stopped||current==null){if(callback!=null)callback.accept(false);return;}
        Profile target=current;long savedRevision=revision;
        JsonObject snapshot=snapshot(target),prefs=codec.preferences(),index=index(current,profiles);
        writing=true;
        disk(()->{writeProfile(target.getJsonFile().toPath(),snapshot);ProfileFiles.write(preferencesFile,prefs);ProfileFiles.write(indexFile,index);return true;},ok->{
            writing=false;if(current==target)writtenRevision=Math.max(writtenRevision,savedRevision);
            persistedPreferences=prefs.toString();failed=false;
            if(callback!=null)callback.accept(true);
        },()->{writing=false;failed=true;if(callback!=null)callback.accept(false);});
    }
    public void shutdown() {
        if(stopped)return;
        JsonObject snapshot=current==null?null:snapshot(current),prefs=codec.preferences(),index=index(current,profiles);
        Future<?> last=writer.submit(()->{try{if(snapshot!=null)writeProfile(current.getJsonFile().toPath(),snapshot);ProfileFiles.write(preferencesFile,prefs);ProfileFiles.write(indexFile,index);}catch(IOException failure){SoarLogger.error("Final profile save failed",failure);}});
        stopped=true;writer.shutdown();
        try{last.get(10,TimeUnit.SECONDS);}catch(Exception failure){SoarLogger.error("Profile writer did not finish",failure);}
    }
    private JsonObject snapshot(Profile profile) {return codec.snapshot(profile.getServerIp(),profile.getType(),profile.getIcon());}
    public void use(Profile target) {use(target,null);}
    public void use(Profile target,Consumer<Boolean> completion) {
        if(working||target==null){if(completion!=null)completion.accept(false);return;}
        if(target==current){if(completion!=null)completion.accept(true);return;}
        working=true;
        disk(()->ProfileCodec.prepare(factory,readProfile(target.getJsonFile().toPath())),prepared->finishSwitch(target,prepared,completion),()->{working=false;if(completion!=null)completion.accept(false);});
    }
    private void finishSwitch(Profile target,JsonObject prepared,Consumer<Boolean> completion) {
        long expected=revision;Profile previous=current;JsonObject rollback=snapshot(previous);
        flush(ok->{
            if(!ok){working=false;if(completion!=null)completion.accept(false);return;}
            if(revision!=expected){working=false;use(target,completion);return;}
            JsonObject nextIndex=index(target,profiles);
            disk(()->{ProfileFiles.write(indexFile,nextIndex);return true;},ignored->{
                if(revision!=expected){working=false;use(target,completion);return;}
                loading=true;
                boolean applied=false;
                try{codec.apply(prepared);current=target;revision++;writtenRevision=revision;failed=false;applied=true;}
                catch(RuntimeException invalid){codec.apply(rollback);current=previous;failed=true;JsonObject oldIndex=index(previous,profiles);writer.execute(()->{try{ProfileFiles.write(indexFile,oldIndex);}catch(IOException failure){SoarLogger.error("Could not restore active profile index",failure);}});}
                finally{loading=false;working=false;}
                if(completion!=null)completion.accept(applied);
            },()->{working=false;if(completion!=null)completion.accept(false);});
        });
    }
    public void create(String name) { create(name,current); }
    public void create(String name,Profile source) {
        if(working||source==null||!validName(name))return;
        String display=name.trim();working=true;
        if(source==current)flush(ok->{if(!ok){working=false;return;}copySource(display,source);});else copySource(display,source);
    }
    private void copySource(String name,Profile source) {
        disk(()->readProfile(source.getJsonFile().toPath()),json->add(name,json,"","","",true,null),()->working=false);
    }
    private void add(String name,JsonObject json,String sourceId,String sourceHash,String stateHash,boolean activate,Consumer<Profile> done) {
        add(name,json,sourceId,sourceHash,stateHash,activate,done,null);
    }
    private void add(String name,JsonObject json,String sourceId,String sourceHash,String stateHash,boolean activate,Consumer<Profile> done,Runnable failure) {
        Profile profile=new Profile(nextId++,"",directory.resolve(UUID.randomUUID()+".json").toFile(),ProfileIcon.GRASS);
        profile.setStableId(UUID.randomUUID().toString());profile.setName(uniqueName(name));profile.setSource(sourceId,sourceHash,stateHash);
        json=ProfileFiles.copy(json);json.add("Profile Data",metadata(profile));final JsonObject saved=json;
        disk(()->{ProfileCodec.prepare(factory,saved);writeProfile(profile.getJsonFile().toPath(),saved);return true;},ignored->{
            profiles.add(profile);working=false;persistIndex();if(done!=null)done.accept(profile);if(activate)use(profile);
        },()->{working=false;if(failure!=null)failure.run();});
    }
    public boolean validName(String name) {
        if(name==null||name.trim().isEmpty()||name.trim().length()>80||name.matches("(?s).*\\p{Cntrl}.*")){failed=true;return false;}
        for(Profile p:profiles)if(p.getName().equalsIgnoreCase(name.trim())){failed=true;return false;}
        return true;
    }
    private String uniqueName(String requested) {
        String name=requested==null||requested.trim().isEmpty()?"Cloud":requested.trim();if(name.length()>65)name=name.substring(0,65);
        String base=name;int suffix=2;while(nameUsed(name))name=base+" ("+(suffix++)+")";return name;
    }
    private boolean nameUsed(String name){for(Profile p:profiles)if(p.getName().equalsIgnoreCase(name))return true;return false;}
    public void rename(Profile profile,String name) {if(working||profile==null||profile.isDefault()||!validName(name))return;profile.setName(name.trim());persistIndex();}
    public void updateMetadata(Profile profile,ProfileType type,ProfileIcon icon,String server) {
        if(working||profile==null)return;working=true;
        disk(()->readProfile(profile.getJsonFile().toPath()),json->{
            ProfileType previousType=profile.getType();ProfileIcon previousIcon=profile.getIcon();String previousServer=profile.getServerIp();
            profile.setType(type);profile.setIcon(icon);profile.setServerIp(server==null?"":server.trim());
            json.add("Profile Data",metadata(profile));
            disk(()->{writeProfile(profile.getJsonFile().toPath(),json);return true;},ignored->{working=false;persistIndex();},()->{profile.setType(previousType);profile.setIcon(previousIcon);profile.setServerIp(previousServer);working=false;});
        },()->working=false);
    }
    public void delete(Profile profile) {
        if(working||profile==null||profile.isDefault())return;
        if(profile==current){use(defaultProfile(),ok->{if(ok)deleteInactive(profile);});return;}deleteInactive(profile);
    }
    private void deleteInactive(Profile profile) {
        working=true;disk(()->{Files.delete(profile.getJsonFile().toPath());Files.deleteIfExists(ProfileFiles.backup(profile.getJsonFile().toPath()));return true;},ok->{profiles.remove(profile);working=false;persistIndex();},()->working=false);
    }
    public void resetDefault() {
        Profile defaults=defaultProfile();if(working||defaults==null)return;working=true;
        JsonObject reset=ProfileFiles.copy(factory);reset.add("Profile Data",metadata(defaults));
        disk(()->{writeProfile(defaults.getJsonFile().toPath(),reset);return true;},ok->{working=false;if(current==defaults){loading=true;try{codec.apply(reset);revision++;writtenRevision=revision;}finally{loading=false;}}else use(defaults);},()->working=false);
    }
    public void importFile(File file) {
        if(working||file==null)return;working=true;
        disk(()->ProfileFiles.read(file.toPath()),json->add(file.getName().replaceFirst("(?i)\\.json$",""),json,"","","",false,null),()->working=false);
    }
    public void addCloud(String title,String sourceId,String sha,JsonObject json,boolean activate,Consumer<Profile> done) {
        addCloud(title,sourceId,sha,json,activate,done,null);
    }
    public void addCloud(String title,String sourceId,String sha,JsonObject json,boolean activate,Consumer<Profile> done,Runnable failure) {
        if(working)return;working=true;
        String expected;
        try{expected=ProfileFiles.hash(ProfileCodec.state(ProfileCodec.prepare(factory,json)));}
        catch(IOException invalid){working=false;failed=true;if(failure!=null)failure.run();return;}
        List<Profile> candidates=new ArrayList<Profile>();for(Profile p:profiles)if(p.getSourceId().equals(sourceId)&&p.getSourceHash().equals(sha))candidates.add(p);
        disk(()->{
            for(Profile p:candidates)try{if(ProfileFiles.hash(ProfileCodec.state(ProfileCodec.prepare(factory,ProfileFiles.read(p.getJsonFile().toPath())))).equals(p.getImportedStateHash()))return p;}catch(IOException invalid){ /* Preserve a damaged/edited copy and create a separate original. */ }
            return null;
        },found->{if(found!=null){working=false;if(activate)use(found);if(done!=null)done.accept(found);}else add(title,json,sourceId,sha,expected,activate,done,failure);},()->{working=false;if(failure!=null)failure.run();});
    }
    public void export(Profile profile,File destination) {readForPublication(profile,json->disk(()->{if(Files.exists(destination.toPath()))throw new IOException("Destination exists");ProfileFiles.write(destination.toPath(),json);return true;},ok->{},null));}
    public void readForPublication(Profile profile,Consumer<JsonObject> ready) {
        readForPublication(profile,ready,null);
    }
    public void readForPublication(Profile profile,Consumer<JsonObject> ready,Runnable failure) {
        if(profile==null||working){if(failure!=null)failure.run();return;}
        Consumer<Boolean> read=ok->{if(ok)disk(()->readProfile(profile.getJsonFile().toPath()),ready,failure);else if(failure!=null)failure.run();};
        if(profile==current)flush(read);else read.accept(true);
    }
    public void load(File file){for(Profile p:profiles)if(p.getJsonFile().equals(file)){use(p);return;}}
    public void upload(File file){importFile(file);}
    public void save(File file,String serverIp,ProfileType type,ProfileIcon icon) {
        for(Profile p:profiles)if(p.getJsonFile().equals(file)){updateMetadata(p,type,icon,serverIp);return;}
        create(file.getName().replaceFirst("(?i)\\.json$",""));
    }
    public void loadProfiles(boolean ignored) { /* All additions go through the manager to retain stable identities. */ }
    private void persistIndex(){JsonObject snapshot=index(current,profiles);disk(()->{ProfileFiles.write(indexFile,snapshot);return true;},ok->{},null);}
    private static JsonObject metadata(Profile profile){JsonObject data=new JsonObject();data.addProperty("Icon",profile.getIcon().getId());data.addProperty("Type",profile.getType().getId());data.addProperty("Server",profile.getServerIp());return data;}
    private JsonObject index(Profile active,Collection<Profile> all) {
        JsonObject out=new JsonObject(),records=new JsonObject();out.addProperty("version",1);if(active!=null)out.addProperty("active",active.getStableId());
        for(Profile p:all){JsonObject record=new JsonObject();record.addProperty("id",p.getStableId());record.addProperty("name",p.getName());record.addProperty("sourceId",p.getSourceId());record.addProperty("sourceHash",p.getSourceHash());record.addProperty("importedStateHash",p.getImportedStateHash());records.add(p.getJsonFile().getName(),record);}
        out.add("profiles",records);return out;
    }
    private JsonObject readProfile(Path path)throws IOException {
        try{JsonObject json=ProfileFiles.read(path);ProfileCodec.prepare(factory,json);return json;}
        catch(IOException invalid){
            Path backup=ProfileFiles.backup(path);if(!Files.isRegularFile(backup))throw invalid;
            JsonObject good=ProfileFiles.read(backup);ProfileCodec.prepare(factory,good);
            if(Files.exists(path))quarantine(path);ProfileFiles.write(path,good);migrationNotice=TranslateText.PROFILE_RECOVERED.getText();return good;
        }
    }
    private void writeProfile(Path path,JsonObject snapshot)throws IOException {
        if(Files.isRegularFile(path))try{ProfileCodec.prepare(factory,ProfileFiles.read(path));}catch(IOException invalid){quarantine(path);}
        ProfileFiles.write(path,snapshot);
    }
    private void quarantine(Path path)throws IOException {Files.move(path,path.resolveSibling(path.getFileName()+".invalid-"+UUID.randomUUID()));}
    private <T> void disk(Disk<T> operation,Consumer<T> success,Runnable failure) {
        if(stopped)return;
        try{writer.execute(()->{try{T result=operation.run();Minecraft.getMinecraft().addScheduledTask(()->{if(stopped)return;if(success!=null)success.accept(result);});}
            catch(Exception problem){SoarLogger.error("Local profile operation failed",problem);Minecraft.getMinecraft().addScheduledTask(()->{failed=true;if(failure!=null)failure.run();});}});}
        catch(RejectedExecutionException closed){failed=true;if(failure!=null)failure.run();}
    }
    private Profile defaultProfile(){for(Profile p:profiles)if(p.isDefault())return p;return null;}
    private static String str(JsonObject object,String key,String fallback){try{return object.has(key)?object.get(key).getAsString():fallback;}catch(RuntimeException invalid){return fallback;}}
    private static int integer(JsonObject object,String key,int fallback){try{return object.has(key)?object.get(key).getAsInt():fallback;}catch(RuntimeException invalid){return fallback;}}
    public Profile getCurrent(){return current;}
    public boolean isWorking(){return working;}
    public boolean isFailed(){return failed;}
    public String getStatus(){return failed?TranslateText.PROFILE_SAVE_FAILED.getText():working?TranslateText.PROFILE_SWITCHING.getText():writing||revision!=writtenRevision?TranslateText.PROFILE_SAVING.getText():TranslateText.PROFILE_SAVED.getText();}
    public String getMigrationNotice(){return migrationNotice;}
    public BackgroundManager getBackgroundManager(){return backgroundManager;}
    public CopyOnWriteArrayList<Profile> getProfiles(){return profiles;}
}
