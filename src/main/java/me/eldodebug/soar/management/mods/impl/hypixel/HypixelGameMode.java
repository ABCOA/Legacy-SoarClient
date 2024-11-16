package me.eldodebug.soar.management.mods.impl.hypixel;

public enum HypixelGameMode {
	SKYWARS_SOLO_NORMAL("/play solo_normal"), SKYWARS_SOLO_INSANE("/play solo_insane"), 
	SKYWARS_DOUBLES_NORMAL("/play teams_normal"), SKYWARS_DOUBLES_INSANE("/play teams_insane"),
	UHC_DUEL_1V1("/play duels_uhc_duel"), UHC_DUEL_2V2("/play duels_uhc_doubles"),
	UHC_DUEL_4V4("/play duels_uhc_four"), UHC_DUEL_MEETUP("/play duels_uhc_meetup"),
	BEDWARS_4V4("/play bedwars_four_four"), BEDWARS_3V3("/play bedwars_four_three"),
	BEDWARS_DOUBLES("/play bedwars_eight_two"), BEDWARS_SOLO("/play bedwars_eight_one"),
	TNT_RUN("/play tnt_tntrun"), PVP_RUN("/play tnt_pvprun"),
	BOW_SPLEEF("/play tnt_bowspleef"), TNT_TAG("/play tnt_tntag"),
	TNT_WIZARDS("/play tnt_capture");
	
	private String command;
	
	private HypixelGameMode(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}
	
	public static HypixelGameMode getModeByCommand(String command) {
		
		for(HypixelGameMode g : HypixelGameMode.values()) {
			if(g.getCommand().equals(command)) {
				return g;
			}
		}
		
		return null;
	}
	
	public static boolean isBedwars(HypixelGameMode mode) {
		return mode.equals(BEDWARS_4V4) || mode.equals(BEDWARS_3V3) || mode.equals(BEDWARS_DOUBLES) || mode.equals(BEDWARS_SOLO);
	}
	
	public static boolean isTntGames(HypixelGameMode mode) {
		return mode.equals(TNT_RUN) || mode.equals(PVP_RUN) || mode.equals(BOW_SPLEEF) 
				|| mode.equals(TNT_TAG) || mode.equals(TNT_WIZARDS);
 	}
}
