/*
 * @(#)LockManager.java 1.0 2004-05-30
 *
 * Copyright (c) Zhong Bo Li. All rights reserved.
 * The software is vested for the exam to Sun Certified Developer for Java 2 and
 * the information contained herein is confidential. You shall not disclose such
 * confidential information and shall use it only in accordance with the written
 * permission from the author.
 */
package com.jingyue.DocConversion.internal;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class manages resource locks, and provides concurrency control. A
 * resource is any shared object. Each resource can be locked only by one owner
 * at the same time. However, it is possible for the same owner to lock more
 * than one resource.
 * 
 * @author Zhong Bo Li
 * @version 1.0
 */
public class LockManager {

	private final List<Lock> waitingLocks = new ArrayList<Lock>();
	private final List<Lock> lockedLocks = new ArrayList<Lock>();
	private final static ReferenceQueue lostLocks = new ReferenceQueue();
	private static MaintenanceThread maintenanceThread = null;
	private static LockManager instance = new LockManager();
	public final static Object ALL_RESOURCE = new Integer(-1);

	private final static Map<String, String> lockIds = Collections.synchronizedMap(new WeakHashMap<String, String>());

	/**
	 * Constructs a lock manager.
	 */
	private LockManager() {
		super();
		maintenanceThread = new MaintenanceThread();
		maintenanceThread.start();
	}

	/**
	 * Retrieves one and only one instance of <code>LockManager</code>.
	 * 
	 * @return the <code>LockManager</code> object
	 */
	public static LockManager getInstance() {
		return instance;
	}

	/**
	 * Returns the lock id with the specified key.
	 * 
	 * @param key the key.
	 * @return the lock id with the specified key.
	 */
	public static String getLockId(String key) {
		String id = lockIds.get(key);

		if (id == null) {
			lockIds.put(key, ("yuntu_lock_id::" + key).intern());
			id = lockIds.get(key);
		}
		return id;
	}

	/**
	 * Request a lock on the specified resource. If the lock must wait, then the
	 * calling thread is blocked until the lock is granted.
	 * 
	 * @param resourceId the ID of the resource to lock for
	 */
	public void lock(Object resourceId) {
		lock(resourceId, Thread.currentThread());
	}

	/**
	 * Request a lock on the specified resource. If the lock must wait, then the
	 * calling thread is blocked until the lock is granted.
	 * 
	 * @param resourceId the ID of the resource to lock for
	 * @param owner      the owner of request lock
	 */
	public void lock(Object resourceId, Object owner) {
		Lock lock = new Lock(resourceId, owner);

		synchronized (lock) {
			if (mustWait(lock)) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					synchronized (this) {
						waitingLocks.remove(lock);
					}
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			} else {
				synchronized (this) {
					lockedLocks.add(lock);
				}
			}
		}
	}

	/**
	 * Determines if the specified lock must wait.
	 * <p>
	 * <li>If the resource in this specified lock has been locked by the same owner,
	 * then this specified lock need not wait;
	 * <li>If the resource in this specified lock has been locked by another owner,
	 * then this specified lock must wait;
	 * <li>If the resource in this specified lock isn't currenly locked by any owner
	 * and at least one ALL_RESOURCE's lock is waiting, then this specified lock
	 * must wait.
	 * 
	 * @param lock the lock to check
	 * @return <tt>true</tt> if this specified lock must wait, <tt>false</tt>
	 *         otherwise
	 */
	private synchronized boolean mustWait(Lock lock) {
		if (isLockedBySameOwner(lock)) {
			return false;
		} else if (isLockedByAnotherOwner(lock)) {
			waitingLocks.add(lock);
			return true;
		} else {
			for (int i = 0; i < waitingLocks.size(); i++) {
				Lock waitingLock = (Lock) waitingLocks.get(i);
				if (waitingLock.getResourceId().equals(ALL_RESOURCE)) {
					waitingLocks.add(lock);
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Release a lock on the specified resource.
	 * 
	 * @param resourceId the ID of the resource to unlock for
	 */

	public synchronized void unlock(Object resourceId) {
		unlock(resourceId, Thread.currentThread());
	}

	/**
	 * Release a lock on the specified resource.
	 * 
	 * @param resourceId the ID of the resource to unlock for
	 * @param owner      the owner of lock
	 */

	public synchronized void unlock(Object resourceId, Object owner) {
		lockedLocks.remove(new Lock(resourceId, owner));
		notifyWaitingLocks(resourceId);
	}

	/**
	 * Release all locks held by the owner.
	 * 
	 * @param owner the owner of request
	 */
	public synchronized void unlockAll(Object owner) {
		int i = 0;

		while (i < lockedLocks.size()) {
			Lock lockedLock = (Lock) lockedLocks.get(i);
			if (lockedLock.getOwner() == null || lockedLock.getOwner().equals(owner)) {
				lockedLocks.remove(i);
			}
		}
		notifyWaitingLocks(ALL_RESOURCE);
	}

	public static void wait(Object obj) {
		wait(obj, -1);
	}

	public static void wait(Object obj, long timeout) {
		synchronized (obj) {
			try {
				if (timeout > 0) {
					obj.wait(timeout);
				} else {
					obj.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void notify(Object obj) {
		synchronized (obj) {
			obj.notify();
		}
	}

	public static void notifyAll(Object obj) {
		synchronized (obj) {
			obj.notifyAll();
		}
	}

	/**
	 * This method causes all thread which are waiting on the specified resource to
	 * wake up. If it finds a ALL_RESOURCE's lock being waiting, then it breaks.
	 * 
	 * @param resourceId the ID of the resource notified
	 */
	private synchronized void notifyWaitingLocks(Object resourceId) {
		int i = 0;

		while (i < waitingLocks.size()) {
			Lock waitingLock = (Lock) waitingLocks.get(i);
			if (waitingLock.getOwner() != null && !isLockedByAnotherOwner(waitingLock)) {
				lockedLocks.add(waitingLocks.remove(i));
				synchronized (waitingLock) {
					waitingLock.notify();
				}
				if (!resourceId.equals(ALL_RESOURCE)) {
					break;
				}
			} else {
				i++;
			}
			if (waitingLock.getResourceId().equals(ALL_RESOURCE)) {
				break;
			}
		}
	}

	/**
	 * This method checks if the resource in this specified lock is currently locked
	 * by another owner. Returns <tt>true</tt> if the resource is locked by another
	 * owner, <tt>false</tt> otherwise.
	 * 
	 * @param lock this specified lock to check
	 * @return <tt>true</tt> if the resource in this specified lock is locked by
	 *         another owner, <tt>false</tt> otherwise
	 */
	private synchronized boolean isLockedByAnotherOwner(Lock lock) {
		int i = 0;

		while (i < lockedLocks.size()) {
			Lock lockedLock = (Lock) lockedLocks.get(i);
			Object owner1 = lock.getOwner();
			Object owner2 = lockedLock.getOwner();

			if ((lock.getResourceId().equals(ALL_RESOURCE) || lockedLock.getResourceId().equals(ALL_RESOURCE)
					|| lock.getResourceId().equals(lockedLock.getResourceId()))
					&& ((owner1 == null && owner2 != null) || !owner1.equals(owner2))) {
				return true;
			}
			i++;
		}
		return false;
	}

	/**
	 * Determines if the resource in this specified lock is currently locked by same
	 * owner. Returns <tt>true</tt> if the resource is locked by same owner,
	 * <tt>false</tt> otherwise.
	 * 
	 * @param lock this specified lock to check
	 * @return <tt>true</tt> if the resource in this specified lock is locked by
	 *         another owner, <tt>false</tt> otherwise
	 */
	private synchronized boolean isLockedBySameOwner(Lock lock) {
		int i = 0;

		while (i < lockedLocks.size()) {
			Lock lockedLock = (Lock) lockedLocks.get(i);
			Object owner1 = lock.getOwner();
			Object owner2 = lockedLock.getOwner();

			if ((lock.getResourceId().equals(ALL_RESOURCE) || lockedLock.getResourceId().equals(ALL_RESOURCE)
					|| lock.getResourceId().equals(lockedLock.getResourceId()))
					&& (owner1 == null || !owner1.equals(owner2))) {
				return false;
			}
			i++;
		}
		return true;
	}

	/**
	 * Determines if the specified resource is currenly locked. Returns
	 * <tt>true</tt> if the resource is locked, <tt>false</tt> otherwise.
	 * 
	 * @param resourceId the ID of the resource to check
	 * @return <tt>true</tt> if the resource is locked, <tt>false</tt> otherwise
	 */
	public synchronized boolean isLocked(Object recsourceId) {
		for (int i = 0; i < lockedLocks.size(); i++) {
			Lock lockedLock = (Lock) lockedLocks.get(i);

			if (lockedLock.getResourceId().equals(ALL_RESOURCE) || lockedLock.getResourceId().equals(recsourceId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * An extension of <code>WeakReference</code> which contains the resourceId and
	 * the <code>owner</code>, a Lock object will be found and be removed after it's
	 * <code>owner</code> been garbage collected.
	 * 
	 * @author Zhong Bo Li
	 * @version 1.0
	 */
	private final class Lock extends WeakReference {
		private final Object resourceId;

		/**
		 * Construct a Lock object, the new weak reference will refer to owner object.
		 */
		Lock(Object resourceId, Object owner) {
			super(owner, lostLocks);
			this.resourceId = resourceId;
		}

		/**
		 * Returns the resource on this <code>Lock</code> object.
		 * 
		 * @return the resource on this <code>Lock</code> object.
		 */
		Object getResourceId() {
			return resourceId;
		}

		/**
		 * Returns the owner of this <code>Lock</code> object.
		 * 
		 * @return the owner of this <code>Lock</code> object.
		 */
		Object getOwner() {
			return get();
		}

		/**
		 * Compares two object for equality. The result is <code>true</code> if and only
		 * if the argument is not <code>null</code> and is a <code>Lock</code> object
		 * that represents the same recNo and owner, as this object.
		 * 
		 * @param obj the object to compare with.
		 * @return <code>true</code> if the objects are the same; <code>false</code>
		 *         otherwise.
		 */
		public boolean equals(Object obj) {
			Object owner1 = getOwner();
			Object owner2 = ((Lock) obj).getOwner();

			return (obj instanceof Lock && getResourceId().equals(((Lock) obj).getResourceId())
					&& ((owner1 != null && owner1.equals(owner2))));
		}
	}

	/**
	 * A thread that avoid any lock is always hold by some crashed owner.
	 */
	private final class MaintenanceThread extends Thread {

		/**
		 * Default public constructor.
		 */
		public MaintenanceThread() {
			super();
			setDaemon(true);
		}

		/**
		 * Remove any lock of the owner has crashed and notify all waiting locks, then
		 * sleeps for one second. This process will continue until the shutdown is
		 * called on the program.
		 */
		public void run() {
			Lock lostLock;
			boolean foundLostLock;

			while (true) {
				foundLostLock = false;
				while ((lostLock = (Lock) lostLocks.poll()) != null) {
					foundLostLock = true;
					synchronized (LockManager.getInstance()) {
						if (waitingLocks.contains(lostLock)) {
							waitingLocks.remove(lostLock);
						}
						if (lockedLocks.contains(lostLock)) {
							lockedLocks.remove(lostLock);
						}
					}
				}
				if (foundLostLock) {
					notifyWaitingLocks(ALL_RESOURCE);
				}
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
}
