package io.trxplorer.searchengine;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import io.redisearch.Schema;
import io.redisearch.client.Client;
import org.jooby.Env;
import org.jooby.Jooby.Module;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;

import java.lang.reflect.Field;

public class RedisModule implements Module {

    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {

        String indexName = conf.getString("redis.index");
        String host = conf.getString("redis.host");
        int port = conf.getInt("redis.port");
        String password = conf.getString("redis.password");

        Client client = new Client(indexName, host, port);
        //反射拿到JedisPool
        Field poolName = Client.class.getDeclaredField("pool");
        JedisPoolConfig poolConf = new JedisPoolConfig();
        poolConf.setMaxTotal(100);
        poolConf.setTestOnBorrow(false);
        poolConf.setTestOnReturn(false);
        poolConf.setTestOnCreate(false);
        poolConf.setTestWhileIdle(false);
        poolConf.setMinEvictableIdleTimeMillis(60000L);
        poolConf.setTimeBetweenEvictionRunsMillis(30000L);
        poolConf.setNumTestsPerEvictionRun(-1);
        poolConf.setFairness(true);
        //反正设置权限和属性
        poolName.setAccessible(true); // 设置私有可访问
        JedisPool pool = new JedisPool(poolConf, host, port, 500, password, 0);
        poolName.set(client, pool);

        Schema sc = new Schema()
                .addTextField("text", 1.0)
                .addNumericField("type");

        try {
            client.createIndex(sc, Client.IndexOptions.Default());
        } catch (JedisDataException e) {
            //FIXME: API doesn't provide a way to handle this properly for now
            if (!e.getMessage().contains("Index already exists")) {
                e.printStackTrace();
            }

        }

        Jedis jedis = new Jedis(host, port);
        //密码设置
        jedis.getClient().setPassword(password);
        binder.bind(Client.class).toInstance(client);
        binder.bind(Jedis.class).toInstance(jedis);

    }


}
