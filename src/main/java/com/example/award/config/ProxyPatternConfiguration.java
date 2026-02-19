package com.example.award.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for Award Management System
 * 
 * PROXY PATTERN CONFIGURATION:
 * ============================
 * 
 * This application uses Spring's dependency injection to implement the Proxy pattern.
 * 
 * How it works:
 * 
 * 1. We have two repository implementations:
 *    - AwardRepo (interface) with a Spring Data implementation (oldFlow bean)
 *    - AwardRepoV2 (interface) with a Spring Data implementation (newFlow bean)
 * 
 * 2. AwardRepoProxy implements the AwardRepo interface and is marked with @Primary
 *    This means when any component asks for AwardRepo, Spring will inject AwardRepoProxy
 * 
 * 3. The actual repository beans are marked with @Qualifier:
 *    - oldFlow repository: @Qualifier("oldFlow")
 *    - newFlow repository: @Qualifier("newFlow")
 * 
 * 4. AwardFacade explicitly injects both repositories using @Qualifier:
 *    @Qualifier("oldFlow") AwardRepo oldFlowRepository
 *    @Qualifier("newFlow") AwardRepoV2 newFlowRepository
 * 
 * 5. Service Layer (AwardService) simply injects AwardRepo:
 *    - It receives AwardRepoProxy (due to @Primary)
 *    - The service layer doesn't know about the proxy - transparent proxying!
 * 
 * Data Flow:
 * 
 * AwardService
 *     ↓ (injects AwardRepo)
 * AwardRepoProxy (@Primary implementation of AwardRepo)
 *     ↓ (delegates to)
 * AwardFacade
 *     ↓ (checks feature toggle)
 *     ├─→ AwardRepo (@Qualifier("oldFlow"))
 *     └─→ AwardRepoV2 (@Qualifier("newFlow"))
 * 
 * Key Benefits:
 * - Service layer code doesn't change
 * - No code changes needed to switch flows
 * - Clean separation of concerns
 * - Feature toggle controls behavior at runtime
 */
@Configuration
public class ProxyPatternConfiguration {
    // This class serves as documentation
    // All actual bean configuration is done via annotations
}
