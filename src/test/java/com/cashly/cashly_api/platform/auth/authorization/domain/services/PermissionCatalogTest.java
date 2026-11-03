package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.shared.infrastructure.i18n.MessageTranslator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionCatalogTest {

    @Test
    void classifies_every_registered_pair_exactly_once() {
        var codes = PermissionCatalog.areas().stream()
                .flatMap(area -> area.submodules().stream())
                .flatMap(submodule -> submodule.domains().stream())
                .flatMap(domain -> domain.permissions().stream())
                .map(PermissionCatalog.PermissionEntry::code)
                .toList();

        assertThat(codes).hasSize(131).doesNotHaveDuplicates();
        assertThat(Set.copyOf(codes)).isEqualTo(PermissionRegistry.codes());
    }

    @Test
    void declares_each_area_once_in_sort_order() {
        assertThat(PermissionCatalog.areas()).extracting(
                PermissionCatalog.AreaCatalog::area)
                .doesNotHaveDuplicates()
                .containsExactly("SALES", "PURCHASES", "CATALOG", "INVENTORY",
                        "FINANCE", "CONTACTS", "ANALYTICS", "PLATFORM");
    }

    @Test
    void groups_finance_into_billing_and_treasury_submodules() {
        var finance = areaOf("FINANCE");

        assertThat(finance.submodules()).extracting(
                PermissionCatalog.SubmoduleCatalog::submodule)
                .containsExactly("FINANCE_BILLING", "FINANCE_TREASURY");
        assertThat(finance.submodules().get(0).domains()).extracting(
                PermissionCatalog.DomainCatalog::module)
                .containsExactly(PermissionModule.FINANCE_BILLING_INVOICES,
                        PermissionModule.FINANCE_BILLING_RETURNS,
                        PermissionModule.FINANCE_BILLING_CREDIT_NOTES,
                        PermissionModule.FINANCE_BILLING_DEBIT_NOTES);
        assertThat(finance.submodules().get(1).domains()).extracting(
                PermissionCatalog.DomainCatalog::module)
                .containsExactly(
                        PermissionModule.FINANCE_TREASURY_CASH_REGISTER,
                        PermissionModule.FINANCE_TREASURY_RECEIVABLES,
                        PermissionModule.FINANCE_TREASURY_PAYMENT_METHODS);
    }

    @Test
    void places_company_under_platform_with_view_and_edit() {
        var platform = areaOf("PLATFORM");
        var company = domainOf(PermissionModule.COMPANY);

        assertThat(platform.submodules().get(0).domains()).extracting(
                PermissionCatalog.DomainCatalog::module)
                .containsExactly(PermissionModule.USERS, PermissionModule.ROLES,
                        PermissionModule.DEPARTMENTS, PermissionModule.COMPANY);
        assertThat(company.permissions()).extracting(
                PermissionCatalog.PermissionEntry::code)
                .containsExactly("COMPANY_VIEW", "COMPANY_EDIT");
    }

    @Test
    void exposes_the_agreed_permission_counts_per_module() {
        Map<PermissionModule, Integer> expected = Map.ofEntries(
                Map.entry(PermissionModule.SALES_QUOTES, 10),
                Map.entry(PermissionModule.SALES_ORDERS, 10),
                Map.entry(PermissionModule.PURCHASES, 9),
                Map.entry(PermissionModule.CATALOG_PRODUCTS, 6),
                Map.entry(PermissionModule.CATALOG_CATEGORIES, 5),
                Map.entry(PermissionModule.CATALOG_PRICES, 5),
                Map.entry(PermissionModule.INVENTORY_WAREHOUSES, 11),
                Map.entry(PermissionModule.FINANCE_BILLING_INVOICES, 10),
                Map.entry(PermissionModule.FINANCE_BILLING_RETURNS, 4),
                Map.entry(PermissionModule.FINANCE_BILLING_CREDIT_NOTES, 4),
                Map.entry(PermissionModule.FINANCE_BILLING_DEBIT_NOTES, 4),
                Map.entry(PermissionModule.FINANCE_TREASURY_CASH_REGISTER, 5),
                Map.entry(PermissionModule.FINANCE_TREASURY_RECEIVABLES, 13),
                Map.entry(PermissionModule.FINANCE_TREASURY_PAYMENT_METHODS, 5),
                Map.entry(PermissionModule.CONTACTS_CUSTOMERS, 7),
                Map.entry(PermissionModule.CONTACTS_SUPPLIERS, 5),
                Map.entry(PermissionModule.DASHBOARD, 1),
                Map.entry(PermissionModule.USERS, 5),
                Map.entry(PermissionModule.ROLES, 5),
                Map.entry(PermissionModule.DEPARTMENTS, 5),
                Map.entry(PermissionModule.COMPANY, 2));

        expected.forEach((module, count) -> assertThat(
                domainOf(module).permissions())
                .as(module.name())
                .hasSize(count));
    }

    @Test
    void exposes_label_and_description_keys_for_every_permission() {
        PermissionCatalog.areas().stream()
                .flatMap(area -> area.submodules().stream())
                .flatMap(submodule -> submodule.domains().stream())
                .flatMap(domain -> domain.permissions().stream())
                .forEach(permission -> {
                    assertThat(permission.labelKey()).isNotBlank();
                    assertThat(permission.descriptionKey())
                            .isEqualTo(permission.labelKey() + ".description");
                });
    }

    @Test
    void orders_areas_submodules_domains_and_permissions_deterministically() {
        var areas = PermissionCatalog.areas();
        assertThat(areas).isSortedAccordingTo(
                Comparator.comparingInt(PermissionCatalog.AreaCatalog::sortOrder)
                        .thenComparing(PermissionCatalog.AreaCatalog::area));
        for (var area : areas) {
            assertThat(area.submodules()).isSortedAccordingTo(
                    Comparator.comparingInt(
                            PermissionCatalog.SubmoduleCatalog::sortOrder));
            area.submodules().forEach(submodule -> {
                assertThat(submodule.domains()).isSortedAccordingTo(
                        Comparator.comparingInt(
                                PermissionCatalog.DomainCatalog::sortOrder));
                submodule.domains().forEach(domain -> assertThat(
                        domain.permissions()).isSortedAccordingTo(
                                Comparator.comparingInt(
                                        PermissionCatalog.PermissionEntry::sortOrder)));
            });
        }
    }

    @Test
    void every_module_is_classified_only_once_across_submodules() {
        var modules = new ArrayList<PermissionModule>();
        PermissionCatalog.areas().forEach(area -> area.submodules()
                .forEach(submodule -> submodule.domains()
                        .forEach(domain -> modules.add(domain.module()))));

        assertThat(modules).hasSize(PermissionModule.values().length)
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrder(PermissionModule.values());
    }

    @Test
    void every_registered_action_is_present_for_its_module() {
        PermissionRegistry.modules().forEach((module, actions) -> {
            var codes = domainOf(module).permissions().stream()
                    .map(permission -> PermissionAction.valueOf(
                            permission.code().substring(
                                    module.name().length() + 1)))
                    .collect(Collectors.toSet());
            assertThat(codes).as(module.name()).isEqualTo(actions);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "es"})
    void every_catalog_key_is_translated_in_both_languages(String language) {
        var translator = new MessageTranslator();
        var locale = Locale.forLanguageTag(language);
        var keys = new HashSet<String>();
        keys.add("platform.auth.areas.sales");
        PermissionCatalog.areas().forEach(area -> {
            keys.add(area.labelKey());
            area.submodules().forEach(submodule -> {
                keys.add(submodule.labelKey());
                submodule.domains().forEach(domain -> {
                    keys.add(domain.labelKey());
                    domain.permissions().forEach(permission -> {
                        keys.add(permission.labelKey());
                        keys.add(permission.descriptionKey());
                    });
                });
            });
        });

        keys.forEach(key -> assertThat(translator.translate(key, locale))
                .as("%s in %s", key, language).isNotBlank());
    }

    private static PermissionCatalog.AreaCatalog areaOf(String area) {
        return PermissionCatalog.areas().stream()
                .filter(candidate -> candidate.area().equals(area)).findFirst()
                .orElseThrow();
    }

    private static PermissionCatalog.DomainCatalog domainOf(
            PermissionModule module) {
        return PermissionCatalog.areas().stream()
                .flatMap(area -> area.submodules().stream())
                .flatMap(submodule -> submodule.domains().stream())
                .filter(domain -> domain.module() == module).findFirst()
                .orElseThrow();
    }
}
