/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.rpc.config;

import com.alipay.sofa.rpc.bootstrap.Bootstraps;
import com.alipay.sofa.rpc.bootstrap.ProviderBootstrap;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.common.utils.ClassUtils;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.ExceptionUtils;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.log.LogCodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

import static com.alipay.sofa.rpc.common.RpcConfigs.getBooleanValue;
import static com.alipay.sofa.rpc.common.RpcConfigs.getIntValue;
import static com.alipay.sofa.rpc.common.RpcConfigs.getStringValue;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_CONCURRENTS;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_DELAY;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_DYNAMIC;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_EXCLUDE;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_INCLUDE;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_INVOKE_TIMEOUT;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_PRIORITY;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_REPEATED_EXPORT_LIMIT;
import static com.alipay.sofa.rpc.common.RpcOptions.PROVIDER_WEIGHT;

/**
 * ?????????????????????
 *
 * @param <T> the type parameter
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
public class ProviderConfig<T> extends AbstractInterfaceConfig<T, ProviderConfig<T>> implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long                                   serialVersionUID    = -3058073881775315962L;

    /*---------- ????????????????????? ------------*/

    /**
     * ?????????????????????
     */
    protected transient T                                       ref;

    /**
     * ?????????????????????
     */
    protected List<ServerConfig>                                server;

    /**
     * ??????????????????,?????????????????????0????????????-1??????spring?????????????????????spring????????????
     */
    protected int                                               delay               = getIntValue(PROVIDER_DELAY);

    /**
     * ??????
     */
    protected int                                               weight              = getIntValue(PROVIDER_WEIGHT);

    /**
     * ???????????????
     */
    protected String                                            include             = getStringValue(PROVIDER_INCLUDE);

    /**
     * ???????????????????????????????????????
     */
    protected String                                            exclude             = getStringValue(PROVIDER_EXCLUDE);

    /**
     * ??????????????????????????????true????????????false????????????????????????????????????????????????????????????
     */
    protected boolean                                           dynamic             = getBooleanValue(PROVIDER_DYNAMIC);

    /**
     * ??????????????????????????????
     */
    protected int                                               priority            = getIntValue(PROVIDER_PRIORITY);

    /**
     * ?????????
     */
    protected String                                            bootstrap;

    /**
     * ??????????????????
     */
    protected transient ThreadPoolExecutor                      executor;

    /**
     * whitelist blacklist
     */

    /*-------- ????????????????????????????????? --------*/

    /**
     * ???????????????????????????(??????)????????????????????????????????????????????????
     */
    protected int                                               timeout             = getIntValue(PROVIDER_INVOKE_TIMEOUT);

    /**
     * ????????????????????????????????????????????????????????????-1??????????????????????????????0?????????????????????????????????
     */
    protected int                                               concurrents         = getIntValue(PROVIDER_CONCURRENTS);

    /**
     * ??????????????????????????????uniqueId???????????????????????????????????????????????????bug??????????????????????????????????????????????????????????????????????????????-1???????????????
     *
     * @since 5.2.0
     */
    protected int                                               repeatedExportLimit = getIntValue(PROVIDER_REPEATED_EXPORT_LIMIT);

    /*---------- ????????????????????? ------------*/

    /**
     * ??????????????????????????????
     */
    protected transient volatile ConcurrentMap<String, Boolean> methodsLimit;

    /**
     * ????????????????????????
     */
    protected transient ProviderBootstrap                       providerBootstrap;

    /**
     * Gets proxy class.
     *
     * @return the proxyClass
     */
    @Override
    public Class<?> getProxyClass() {
        if (proxyClass != null) {
            return proxyClass;
        }
        try {
            if (StringUtils.isNotBlank(interfaceId)) {
                this.proxyClass = ClassUtils.forName(interfaceId);
                if (!proxyClass.isInterface()) {
                    if ((getServer() != null) && getServer().size() != 0) {
                        for (int i = 0; i < getServer().size(); i++) {
                            if (!RpcConstants.PROTOCOL_TYPE_TRIPLE.equals(getServer().get(i).getProtocol())) {
                                throw ExceptionUtils.buildRuntime("service.interfaceId",
                                    interfaceId, "interfaceId must set interface class, not implement class");
                            }
                        }
                    }
                }
            } else {
                throw ExceptionUtils.buildRuntime("service.interfaceId",
                    "null", "interfaceId must be not null");
            }
        } catch (SofaRpcRuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_GET_PROXY_CLASS), e);
        }
        return proxyClass;
    }

    /**
     * Build key.
     *
     * @return the string
     */
    @Override
    public String buildKey() {
        return interfaceId + ":" + uniqueId;
    }

    /**
     * Gets ref.
     *
     * @return the ref
     */
    public T getRef() {
        return ref;
    }

    /**
     * Sets ref.
     *
     * @param ref the ref
     * @return the ref
     */
    public ProviderConfig<T> setRef(T ref) {
        this.ref = ref;
        return this;
    }

    /**
     * Gets server.
     *
     * @return the server
     */
    public List<ServerConfig> getServer() {
        return server;
    }

    /**
     * Sets server.
     *
     * @param server the server
     * @return the server
     */
    public ProviderConfig<T> setServer(List<ServerConfig> server) {
        this.server = server;
        return this;
    }

    /**
     * Gets delay.
     *
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public ProviderConfig<T> setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    /**
     * Gets weight.
     *
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Sets weight.
     *
     * @param weight the weight
     * @return the weight
     */
    public ProviderConfig<T> setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Gets include.
     *
     * @return the include
     */
    public String getInclude() {
        return include;
    }

    /**
     * Sets include.
     *
     * @param include the include
     * @return the include
     */
    public ProviderConfig<T> setInclude(String include) {
        this.include = include;
        return this;
    }

    /**
     * Gets exclude.
     *
     * @return the exclude
     */
    public String getExclude() {
        return exclude;
    }

    /**
     * Sets exclude.
     *
     * @param exclude the exclude
     * @return the exclude
     */
    public ProviderConfig<T> setExclude(String exclude) {
        this.exclude = exclude;
        return this;
    }

    /**
     * Is dynamic boolean.
     *
     * @return the boolean
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Sets dynamic.
     *
     * @param dynamic the dynamic
     * @return the dynamic
     */
    public ProviderConfig<T> setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     * @return the priority
     */
    public ProviderConfig<T> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets bootstrap.
     *
     * @return the bootstrap
     */
    public String getBootstrap() {
        return bootstrap;
    }

    /**
     * Sets bootstrap.
     *
     * @param bootstrap the bootstrap
     * @return the bootstrap
     */
    public ProviderConfig<T> setBootstrap(String bootstrap) {
        this.bootstrap = bootstrap;
        return this;
    }

    /**
     * Gets executor.
     *
     * @return the executor
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * Sets executor.
     *
     * @param executor the executor
     * @return the executor
     */
    public ProviderConfig<T> setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Gets concurrents.
     *
     * @return the concurrents
     */
    public int getConcurrents() {
        return concurrents;
    }

    /**
     * Sets concurrents.
     *
     * @param concurrents the concurrents
     * @return the concurrents
     */
    public ProviderConfig<T> setConcurrents(int concurrents) {
        this.concurrents = concurrents;
        return this;
    }

    /**
     * Gets repeated export limit.
     *
     * @return the repeated export limit
     */
    public int getRepeatedExportLimit() {
        return repeatedExportLimit;
    }

    /**
     * Sets repeated export limit.
     *
     * @param repeatedExportLimit the repeated export limit
     * @return the repeated export limit
     */
    public ProviderConfig<T> setRepeatedExportLimit(int repeatedExportLimit) {
        this.repeatedExportLimit = repeatedExportLimit;
        return this;
    }

    /**
     * Gets client timeout.
     *
     * @return the client timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets client timeout.
     *
     * @param timeout the client timeout
     * @return the client timeout
     */
    public ProviderConfig setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public boolean hasTimeout() {
        if (timeout > 0) {
            return true;
        }
        if (CommonUtils.isNotEmpty(methods)) {
            for (MethodConfig methodConfig : methods.values()) {
                if (methodConfig.getTimeout() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ???????????????????????????????????????????????????
     * ??????-1??????????????????????????????0?????????????????????????????????
     *
     * @return ???????????????concurrents boolean
     */
    @Override
    public boolean hasConcurrents() {
        if (concurrents > 0) {
            return true;
        }
        if (CommonUtils.isNotEmpty(methods)) {
            for (MethodConfig methodConfig : methods.values()) {
                if (methodConfig.getConcurrents() != null
                    && methodConfig.getConcurrents() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * add server.
     *
     * @param server ServerConfig
     * @return the ProviderConfig
     */
    public ProviderConfig<T> setServer(ServerConfig server) {
        if (this.server == null) {
            this.server = new ArrayList<ServerConfig>();
        }
        this.server.add(server);
        return this;
    }

    /**
     * ????????????????????????????????????
     *
     * @return ?????????????????????????????? methods limit
     */
    public Map<String, Boolean> getMethodsLimit() {
        return methodsLimit;
    }

    /**
     * Sets methodsLimit.
     *
     * @param methodsLimit the methodsLimit
     * @return the ProviderConfig
     */
    public ProviderConfig<T> setMethodsLimit(ConcurrentMap<String, Boolean> methodsLimit) {
        this.methodsLimit = methodsLimit;
        return this;
    }

    /**
     * ????????????
     */
    public synchronized void export() {
        if (providerBootstrap == null) {
            providerBootstrap = Bootstraps.from(this);
        }
        providerBootstrap.export();
    }

    /**
     * ??????????????????
     */
    public synchronized void unExport() {
        if (providerBootstrap != null) {
            providerBootstrap.unExport();
        }
    }

    /**
     * ??????????????????????????????
     *
     * @return bootstrap bootstrap
     */
    public ProviderBootstrap getProviderBootstrap() {
        return providerBootstrap;
    }

    /**
     * set provider bootstrap
     *
     * @param providerBootstrap
     */
    public void setProviderBootstrap(ProviderBootstrap providerBootstrap) {
        this.providerBootstrap = providerBootstrap;
    }
}
