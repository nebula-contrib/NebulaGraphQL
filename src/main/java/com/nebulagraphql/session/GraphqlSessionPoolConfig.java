package com.nebulagraphql.session;

import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import java.util.List;

public class GraphqlSessionPoolConfig{
    private SessionPoolConfig sessionPoolConfig;
    private List<HostAddress> metadAddress;

    public GraphqlSessionPoolConfig(
        List<HostAddress> graphdAddresses,
        List<HostAddress> metadAddresses,
        String spaceName,
        String username,
        String password
    ){
        if(metadAddresses == null || metadAddresses.size()==0){
            throw new IllegalArgumentException("Metad addresses cannot be empty.");
        }
        this.sessionPoolConfig = new SessionPoolConfig(graphdAddresses, spaceName, username, password);
        this.metadAddress = metadAddresses;
    }

    

    public SessionPoolConfig getSessionPoolConfig() {
        return sessionPoolConfig;
    }



    public void setSessionPoolConfig(SessionPoolConfig sessionPoolConfig) {
        this.sessionPoolConfig = sessionPoolConfig;
    }



    public List<HostAddress> getMetadAddress() {
        return metadAddress;
    }



    public void setMetadAddress(List<HostAddress> metadAddress) {
        this.metadAddress = metadAddress;
    }



    public String getUsername() {
        return sessionPoolConfig.getUsername();
    }

    public String getPassword() {
        return sessionPoolConfig.getPassword();
    }

    public List<HostAddress> getGraphAddressList() {
        return sessionPoolConfig.getGraphAddressList();
    }

    public String getSpaceName() {
        return sessionPoolConfig.getSpaceName();
    }

    public int getMinSessionSize() {
        return sessionPoolConfig.getMinSessionSize();
    }

    public GraphqlSessionPoolConfig setMinSessionSize(int minSessionSize){
        sessionPoolConfig.setMinSessionSize(minSessionSize);
        return this;
    }

    public int getMaxSessionSize() {
        return sessionPoolConfig.getMaxSessionSize();
    }

    public GraphqlSessionPoolConfig setMaxSessionSize(int maxSessionSize){
        sessionPoolConfig.setMaxSessionSize(maxSessionSize);
        return this;
    }

    public int getTimeout() {
        return sessionPoolConfig.getTimeout();
    }

    public GraphqlSessionPoolConfig setTimeout(int timeout){
        sessionPoolConfig.setTimeout(timeout);
        return this;
    }

    public int getCleanTime(){
        return sessionPoolConfig.getCleanTime();
    }

    public GraphqlSessionPoolConfig setCleanTime(int cleanTime){
        sessionPoolConfig.setCleanTime(cleanTime);
        return this;
    }

    public int getHealthCheckTime(){
        return sessionPoolConfig.getHealthCheckTime();
    }

    public GraphqlSessionPoolConfig setHealthCheckTime(int healthCheckTime){
        sessionPoolConfig.setHealthCheckTime(healthCheckTime);
        return this;
    }

    public int getWaitTime(){
        return sessionPoolConfig.getWaitTime();
    }

    public GraphqlSessionPoolConfig setWaitTime(int waitTime){
        sessionPoolConfig.setWaitTime(waitTime);
        return this;
    }

    @Override
    public String toString(){
        return "GraphqlSessionPoolConfig{"
                + "username='" + sessionPoolConfig.getUsername() + '\''
                + ", graphAddressList=" + sessionPoolConfig.getGraphAddressList()
                + ", metadAddressList=" + metadAddress
                + ", spaceName='" + sessionPoolConfig.getSpaceName() + '\''
                + ", minSessionSize=" + sessionPoolConfig.getMinSessionSize()
                + ", maxSessionSize=" + sessionPoolConfig.getMaxSessionSize()
                + ", timeout=" + sessionPoolConfig.getTimeout()
                + ", idleTime=" + sessionPoolConfig.getCleanTime()
                + ", healthCheckTime=" + sessionPoolConfig.getHealthCheckTime()
                + ", waitTime=" + sessionPoolConfig.getWaitTime()
                + '}';
    }
}
