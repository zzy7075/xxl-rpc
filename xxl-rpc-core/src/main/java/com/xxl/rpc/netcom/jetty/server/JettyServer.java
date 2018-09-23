package com.xxl.rpc.netcom.jetty.server;

import com.xxl.rpc.netcom.common.server.IServer;
import com.xxl.rpc.serialize.Serializer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc jetty server
 * @author xuxueli 2015-11-19 22:29:03
 *
 * 	<code>
		<!-- JETTY RPC, 服务端配置(类似Hessian B-RPC, +注册功能) -->
		<bean class="com.xxl.rpc.netcom.NetComServerFactory">
			<property name="port" value="7080" />
			<property name="netcom" value="JETTY" />
			<property name="serializer" value="HESSIAN" />
			<property name="zookeeper_switch" value="false" />
		</bean>
 * 	</code>
 */
public class JettyServer extends IServer {
	private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);

	private Server server;

	@Override
	public void start(final int port, final Serializer serializer) throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {

				// The Server
				server = new Server(new ExecutorThreadPool(1000));

				// HTTP connector
				ServerConnector connector = new ServerConnector(server);
				connector.setPort(port);
				server.setConnectors(new Connector[]{connector});

				// Set a handler
				HandlerCollection handlerc =new HandlerCollection();
				handlerc.setHandlers(new Handler[]{new JettyServerHandler(serializer)});
				server.setHandler(handlerc);

				try {
					server.start();
					logger.info(">>>>>>>>>>> xxl-rpc server start success, netcon={}, port={}", JettyServer.class.getName(), port);
					server.join();
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					server.destroy();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void destroy() throws Exception {
		if (server != null) {
			try {
				server.destroy();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		logger.info(">>>>>>>>>>> xxl-rpc server destroy success, netcon={}", JettyServer.class.getName());
	}
}
