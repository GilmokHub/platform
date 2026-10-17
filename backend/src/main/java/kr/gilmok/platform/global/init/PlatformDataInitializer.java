package kr.gilmok.platform.global.init;

import kr.gilmok.platform.queue.dto.QueueCreateRequest;
import kr.gilmok.platform.queue.repository.QueueRepository;
import kr.gilmok.platform.queue.service.AdminQueueService;
import kr.gilmok.platform.tenant.entity.Tenant;
import kr.gilmok.platform.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PlatformDataInitializer implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final QueueRepository queueRepository;
    private final AdminQueueService adminQueueService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Tenant defaultTenant;
        if (tenantRepository.count() == 0) {
            defaultTenant = tenantRepository.save(Tenant.builder()
                    .companyName("길목 데모 티켓팅 (demo-tenant)")
                    .adminEmail("admin@demo.gilmok.kr")
                    .apiKeyHash("gmk_demo_dev_2026")
                    .planTier("ENTERPRISE")
                    .status("ACTIVE")
                    .build());
            log.info("[PlatformDataInitializer] 기본 테넌트 시드 생성 완료: id={}, name={}",
                    defaultTenant.getId(), defaultTenant.getCompanyName());
        } else {
            defaultTenant = tenantRepository.findAll().get(0);
        }

        if (queueRepository.count() == 0) {
            var createdQueue = adminQueueService.createQueue(
                    defaultTenant.getId(),
                    new QueueCreateRequest("아이유 콘서트 2026 대기열", "DEMO-CONCERT-2026")
            );
            log.info("[PlatformDataInitializer] 기본 대기열 및 정책 시드 생성 완료: queueId={}, queueCode={}",
                    createdQueue.queueId(), createdQueue.queueCode());
        }
    }
}
