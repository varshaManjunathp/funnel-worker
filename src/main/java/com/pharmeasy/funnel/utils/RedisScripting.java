package com.pharmeasy.funnel.utils;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@AllArgsConstructor
public class RedisScripting {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    HashMap<String, String> luaScripts;

    @Value("${env}")
    private String environment;

    RedisScripting() {
        this.luaScripts = new HashMap<>();
    }
    @PostConstruct
    private void loadScripts() throws IOException {

        File folder = new File("src/main/resources/lua");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".lua")) {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                luaScripts.put(FilenameUtils.removeExtension(file.getName()).toUpperCase(Locale.ROOT), content);
            }
        }
    }

    public Object execute(String script, String key, Object... args) {
        RedisScript redisScript = RedisScript.of(this.luaScripts.get(script));
        redisTemplate.setEnableTransactionSupport(false);
         return redisTemplate.execute(redisScript, Collections.singletonList(key), args);
    }

    public String getSegmentType() {
        String script = "TYPE";
        return ObjectUtils.nullSafeToString(execute(script, getSetKeyName()));
    }

    public String bitsetRemove(List<String> entityIds) {
        String script = "REMOVE";
        return ObjectUtils.nullSafeToString(execute(script, getSetKeyName(), entityIds));
    }

    public String bitsetAdd(List<String> entityIds) {
        String script = "BITSET_ADD";
        return ObjectUtils.nullSafeToString(execute(script, getSetKeyName(), entityIds));
    }

    private String getSetKeyName() {
        return environment+":funnel:worker";
    }

    private String getTempKeyName() {
        return environment+":funnel:worker";
    }
}
