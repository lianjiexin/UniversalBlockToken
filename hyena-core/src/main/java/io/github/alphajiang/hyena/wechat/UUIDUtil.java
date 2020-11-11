package io.github.alphajiang.hyena.wechat;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UUIDUtil {
	private static final int LISTSIZE = 500;

	private static final List<String> UUID_LIST = Collections

	.synchronizedList(new LinkedList<String>());

	public static String getUUID() {

		if (UUID_LIST.size() == 0) {

			for (int i = 0; i < LISTSIZE; ++i) {

				String s = UUID.randomUUID().toString().replaceAll("-", "");

				UUID_LIST.add(s);

			}

		}
		return UUID_LIST.remove(0);

	}

	public static void main(String[] args) throws Exception {

		ExecutorService threadPool = Executors.newFixedThreadPool(50);

		for (int i = 0; i < 10000; i++)

			threadPool.execute(new Runnable() {

				public void run() {

					for (int j = 0; j < 1; j++)

						System.out.println(getUUID());

				}

			});

		threadPool.shutdown();

	}
}