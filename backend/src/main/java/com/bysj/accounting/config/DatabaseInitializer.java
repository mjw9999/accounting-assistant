package com.bysj.accounting.config;

import com.bysj.accounting.domain.Admin;
import com.bysj.accounting.domain.CommonStatus;
import com.bysj.accounting.repository.AdminRepository;
import com.bysj.accounting.service.PasswordSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final AdminRepository adminRepository;
    private final PasswordSupport passwordSupport;

    public DatabaseInitializer(AdminRepository adminRepository, PasswordSupport passwordSupport) {
        this.adminRepository = adminRepository;
        this.passwordSupport = passwordSupport;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminRepository.count() == 0) {
            log.info("发现数据库中没有管理员账号，正在自动创建默认系统管理员...");
            Admin admin = new Admin();
            admin.username = "admin";
            admin.password = passwordSupport.encode("admin123");
            admin.realName = "系统管理员";
            admin.phone = "13800000000";
            admin.status = CommonStatus.ENABLED;
            adminRepository.save(admin);
            log.info("默认管理员账号创建成功！账号：admin，密码：admin123");
        }
    }
}
