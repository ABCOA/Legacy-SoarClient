package me.eldodebug.soar.management.event.impl;

import me.eldodebug.soar.management.event.Event;
import net.minecraft.scoreboard.ScoreObjective;

public class EventRenderScoreboard extends Event {

	private ScoreObjective objective;
	
	public EventRenderScoreboard(ScoreObjective objective) {
		this.objective = objective;
	}

	public ScoreObjective getObjective() {
		return objective;
	}
}