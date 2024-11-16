package me.eldodebug.soar.utils;

import io.netty.util.internal.ThreadLocalRandom;

public class RandomUtils {

	public static int getRandomInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
	
	public static long getRandomLong(int min, int max) {
		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}
}
