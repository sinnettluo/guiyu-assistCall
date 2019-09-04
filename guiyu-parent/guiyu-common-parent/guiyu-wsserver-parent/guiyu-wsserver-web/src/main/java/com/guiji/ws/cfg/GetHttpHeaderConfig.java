package com.guiji.ws.cfg;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

/**
 * @author Zhouzl
 * @date 2019年07月01日
 * @since 1.0
 */
public class GetHttpHeaderConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        super.modifyHandshake(sec, request, response);
        Map<String, List<String>> headers = request.getHeaders();
        if(headers != null){
            sec.getUserProperties().put("headers", headers);
        }
    }
}
