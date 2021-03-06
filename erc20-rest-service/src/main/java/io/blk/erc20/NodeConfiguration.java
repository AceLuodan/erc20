package io.blk.erc20;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Node configuration bean.
 */
@Data
@ConfigurationProperties("io.blk.erc20")
@Component
public class NodeConfiguration {

    private String nodeEndpoint = System.getProperty("nodeEndpoint");
    private String fromAddress = System.getProperty("fromAddress");

    public String getNodeEndpoint() {
        return nodeEndpoint;
    }

    public void setNodeEndpoint(String nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
