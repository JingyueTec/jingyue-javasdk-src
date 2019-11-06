/*
 * @(#)ThreadPool.java 1.0 2005-3-6
 *
 * 作者：李忠波
 */
package com.jingyue.DocConversion.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 李忠波
 * @version 1.0
 */
public class ThreadPool {
	static final long IDLE_TIMEOUT = 60000L;

	private final Lock lock = new ReentrantLock();

	private String name;
	private int minsize;
	private int maxsize;
	private int nextWorkerId = 0;
	private LinkedList<Worker> pool = new LinkedList<Worker>();
	private static ThreadPool instance = null;
	private static final List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>());

	private ThreadPool() {
		this("JPingThreadPool"); //$NON-NLS-1$
	}

	private ThreadPool(String name) {
		this(name, 0, Runtime.getRuntime().availableProcessors() * 3 / 2 + 1);
	}

	private ThreadPool(String name, int minsize, int maxsize) {
		this.name = name;
		this.minsize = minsize;
		this.maxsize = maxsize;
	}

	public static ThreadPool getInstance() {
		if (instance == null) {
			instance = new ThreadPool();
		}
		return instance;
	}

	public Thread run(Runnable runner) {
		return run(runner, Thread.NORM_PRIORITY);
	}

	public Thread run(Runnable runner, int level) {
		Worker worker = null;

		if (runner == null) {
			throw new NullPointerException();
		}

		boolean isEmpty = false;

		lock.lock();
		try {
			isEmpty = pool.isEmpty();
			if (!isEmpty) {
				worker = pool.removeFirst();
			}
		} finally {
			lock.unlock();
		}
		if (isEmpty) {
			worker = new Worker(name + "-" + ++nextWorkerId); //$NON-NLS-1$
			worker.start();
		}

		worker.setPriority(level);

		// ...and wake up worker to service incoming runner
		worker.wakeup(runner);
		threads.add(worker);
		return worker;
	}

	public static void stopAll() {
		while (!threads.isEmpty()) {
			try {
				threads.remove(0).interrupt();
			} catch (OutOfMemoryError err) {
				err.printStackTrace();
				throw err;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		instance = null;
	}

	// Notified when a worker has idled timeout
	// @return true if worker should die, false otherwise
	boolean notifyTimeout(Worker worker) {
		if (worker.runner != null) {
			return false;
		}
		lock.lock();
		try {
			if (pool.size() > minsize) {
				// Remove from free list
				pool.remove(worker);
				return true; // die
			}
		} finally {
			lock.unlock();
		}
		return false; // continue
	}

	// Notified when a worker has finished his work and
	// free to service next runner
	// @return true if worker should die, false otherwise
	boolean notifyFree(Worker worker) {
		lock.lock();
		try {
			if (pool.size() < maxsize) {
				// Add to free list
				pool.addLast(worker);
				return false; // continue
			}
		} finally {
			lock.unlock();
		}
		return true; // die
	}

	// The inner class that implement worker thread
	class Worker extends Thread {
		private final Lock lock = new ReentrantLock();
		private final Condition condition = lock.newCondition();
		private boolean isStoped = false;
		Runnable runner = null;

		public Worker(String name) {
			super(name);
		}

		void wakeup(Runnable runner) {
			lock.lock();
			try {
				this.runner = runner;
				condition.signal();
			} finally {
				lock.unlock();
			}
		}

		@Override
		public void interrupt() {
			super.interrupt();
			this.isStoped = true;
		}

		public void run() {
			for (;;) {
				lock.lock();
				try {
					if (runner == null) {
						try {
							condition.await(/* IDLE_TIMEOUT */);
						} catch (InterruptedException e) {
							break;
						}
					}
				} finally {
					lock.unlock();
				}

				// idle timed out, die or put into free list
				if (runner == null) {
					if (notifyTimeout(this))
						break;
					else
						continue;
				}

				try {
					try {
						runner.run();
					} catch (OutOfMemoryError err) {
						runner = null;
						notifyTimeout(this);
						err.printStackTrace();
						throw err;
					} catch (Throwable err) {
						err.printStackTrace();
					}
				} finally {
					runner = null;
					if (this.isStoped || notifyFree(this)) {
						break;
					}
				}
			}
		}
	}
}
