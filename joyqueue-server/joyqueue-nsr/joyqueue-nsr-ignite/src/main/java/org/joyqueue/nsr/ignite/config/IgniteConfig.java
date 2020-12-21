/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.ignite.config;

import org.joyqueue.toolkit.config.PropertySupplier;

public class IgniteConfig {

    public static final String STORAGE_STORE_PATH = "/ignite/cache/nameserver/persisted";
    public static final String STORAGE_WAL_PATH = "/ignite/cache/nameserver/wal";
    public static final String STORAGE_WAL__ARCHIVE_PATH = "/ignite/cache/nameserver/archive";
    private PropertySupplier propertySupplier;
    private String storePath;
    private String walPath;
    private String walArchivePath;

    public IgniteConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public String getWalPath() {
        return walPath;
    }

    public void setWalPath(String walPath) {
        this.walPath = walPath;
    }

    public String getWalArchivePath() {
        return walArchivePath;
    }

    public void setWalArchivePath(String walArchivePath) {
        this.walArchivePath = walArchivePath;
    }

    public boolean clientMode() {
        return false;
    }


/*
    CLIENT_MODE("nameserver.ignite.clientMode",false,Type.BOOLEAN),
    PERR_CLASS_LOADING_ENABLED("nameserver.ignite.peerClassLoadingEnabled",true,Type.BOOLEAN),
    DEPLOYMENT_MODE("nameserver.ignite.deploymentMode", "SHARD",Type.STRING),
    PEER_CLASS_LOADING_MISSED_RESOURCE_CACHE_SIZE("nameserver.ignite.peerClassLoadingMissedResourcesCacheSize",0,Type.INT),
    NETWORK_TIMEOUT("nameserver.ignite.networkTimeout",10000,Type.INT),
    PUBLIC_THREAD_POOL_SIZE("nameserver.ignite.publicThreadPoolSize", 32,Type.INT),
    SYSTEM_THREAD_POOL_SIZE("nameserver.ignite.systemThreadPoolSize", 16,Type.INT),
    BINARY_COMPACT_FOOTER("nameserver.ignite.binary.compactFooter",true,Type.BOOLEAN),

    CACHE_NAME("nameserver.ignite.cache.name","IGNITE_CACHE_KEY_PCOMM_RATE",Type.STRING),
    CACHE_MODE("nameserver.ignite.cache.cacheMode", "REPLICATED",Type.STRING),
    CACHE_ATOMICITY_MODE("nameserver.ignite.cache.atomicityMode", "TRANSACTIONAL",Type.STRING),
    CACHE_BACKUPS("nameserver.ignite.cache.backups", 1,Type.INT),
    CACHE_COPY_READ("nameserver.ignite.cache.copyOnRead",false,Type.BOOLEAN),
    CACHE_DATA_REGION_NAME("nameserver.ignite.cache.dataRegionName",null,Type.BOOLEAN),
    CACHE_STORE_KEEP_BINARY("nameserver.ignite.cache.storeKeepBinary", true,Type.BOOLEAN),

    STORAGE_DEFAULT_DATA_REGION_NAME("nameserver.ignite.storage.defaultDataRegion.name",null,Type.STRING),
    STORAGE_DEFAULT_DATA_REGION_MAX_SIZE("nameserver.ignite.storage.defaultDataRegion.maxSize",536870912,Type.INT),
    STORAGE_DEFAULT_DATA_REGION_INITIAL_SIZE("nameserver.ignite.storage.defaultDataRegion.initialSize",107374182,Type.INT),
    STORAGE_DEFAULT_DATA_REGION_PERSISTENCE_ENABLED("nameserver.ignite.storage.defaultDataRegion.persistenceEnabled",true,Type.BOOLEAN),

    UDF_REGION_NAME("nameserver.ignite.storage.udfDataRegion.name","udfRegion", Type.STRING),
    UDF_REGION_MAX_SIZE("nameserver.ignite.storage.udfDataRegion.maxSize",536870912, Type.INT),
    UDF_REGION_INITIAL_SIZE("nameserver.ignite.storage.udfDataRegion.initialSize",536870912, Type.INT),
    UDF_REGION_PERSISTENCE_ENABLED("nameserver.ignite.storage.udfDataRegion.persistenceEnabled",true, Type.BOOLEAN),
    STORAGE_WAL_MODE("nameserver.ignite.storage.walMode","FSYNC", Type.STRING),
    STORAGE_CHECKPOINT_FREQUENCY("nameserver.ignite.storage.checkpointFrequency",10000, Type.INT),
    STORAGE_CHECKPOINT_THREADS("nameserver.ignite.storage.checkpointThreads",4, Type.INT),
    STORAGE_WAL_HISTORY_SIZE("nameserver.ignite.storage.walHistorySize",20, Type.INT),

    DISCOVERY_SPI_LOCAL_PORT("nameserver.ignite.discoverySpi.localPort", 48500,Type.INT),
    DISCOVERY_SPI_LOCAL_PORT_RANGE("nameserver.ignite.discoverySpi.localPortRange", 20,Type.INT),
    DISCOVERY_SPI_JOIN_TIMEOUT("nameserver.ignite.discoverySpi.joinTimeout", 0,Type.INT),
    DISCOVERY_SPI_NETWORK_TIMEOUT("nameserver.ignite.discoverySpi.networkTimeout", 5000,Type.INT),
    DISCOVERY_SPI_IP_FINDER_ADDRESS("nameserver.ignite.discoverySpi.ipFinder.address", null,Type.STRING),
    DISCOVERY_SPI_COMMUNICATION_SPI_LOCAL_PORT("nameserver.ignite.communicationSpi.localPort", 48100,Type.INT);*/


}
