package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class PermissionCatalogTest {

    private static final int TEST_ORDER = 10;
    private static final String NAVBAR_TREE = "/permissions/navbar-permission-tree.json";

    @Test
    void classifies_every_registered_pair_exactly_once() {
        assertThat(classifiedActions()).isEqualTo(PermissionRegistry.modules());
    }

    @Test
    void does_not_classify_any_unregistered_pair() {
        var registered = PermissionRegistry.modules();

        classifiedActions().forEach((module, actions) -> assertThat(actions)
                .as(module.name())
                .isEqualTo(registered.get(module)));
    }

    @Test
    void declares_each_domain_exactly_once() {
        var domains = PermissionCatalog.areas().stream()
                .flatMap(area -> area.domains().stream())
                .map(PermissionCatalog.DomainCatalog::module).toList();

        assertThat(domains).doesNotHaveDuplicates()
                .containsExactlyInAnyOrder(BusinessModule.values());
    }

    @Test
    void business_modules_match_the_navbar_contract() {
        assertThat(BusinessModule.values()).extracting(Enum::name)
.containsExactlyInAnyOrder(
                "USERS", "ROLES", "DEPARTMENTS", 
                "CATALOG_PRODUCTS", "CATALOG_CATEGORIES", "CATALOG_PRICES", 
                "INVENTORY_WAREHOUSES", "SALES_QUOTES", "SALES_ORDERS", 
                "CONTACTS_CUSTOMERS", "FINANCE_INVOICES", "FINANCE_RETURNS", 
                "FINANCE_CREDIT_NOTES", "FINANCE_DEBIT_NOTES", "FINANCE_CASH_REGISTER", 
                "FINANCE_RECEIVABLES", "FINANCE_PAYMENT_METHODS", "PURCHASES", 
                "CONTACTS_SUPPLIERS", "DASHBOARD");
    }

    @Test
    void declares_each_area_once_in_sort_order() {
        var areas = PermissionCatalog.areas();

        assertThat(areas).extracting(PermissionCatalog.AreaCatalog::area)
                .doesNotHaveDuplicates()
                .containsExactly("SALES", "PURCHASES", "CATALOG", "INVENTORY",
                        "FINANCE", "CONTACTS", "ANALYTICS", "PLATFORM");
    }

    @Test
    void assigns_area_sort_orders_following_the_navbar() {
        assertThat(PermissionCatalog.areas())
                .extracting(PermissionCatalog.AreaCatalog::area,
                        PermissionCatalog.AreaCatalog::sortOrder)
                .containsExactly(
                        tuple("SALES", 10),
                        tuple("PURCHASES", 20),
                        tuple("CATALOG", 30),
                        tuple("INVENTORY", 40),
                        tuple("FINANCE", 50),
                        tuple("CONTACTS", 60),
                        tuple("ANALYTICS", 70),
                        tuple("PLATFORM", 90));
    }

    @Test
    void orders_domains_within_each_area_like_the_navbar() {
        var modulesByArea = PermissionCatalog.areas().stream()
                .collect(Collectors.toMap(
                        PermissionCatalog.AreaCatalog::area,
                        area -> area.domains().stream()
                                .map(domain -> domain.module().name())
                                .toList()));

        assertThat(modulesByArea).containsEntry("SALES",
                List.of("SALES_QUOTES", "SALES_ORDERS"));
        assertThat(modulesByArea).containsEntry("PURCHASES",
                List.of("PURCHASES"));
        assertThat(modulesByArea).containsEntry("CATALOG",
                List.of("CATALOG_PRODUCTS", "CATALOG_CATEGORIES", "CATALOG_PRICES"));
        assertThat(modulesByArea).containsEntry("INVENTORY",
                List.of("INVENTORY_WAREHOUSES"));
        assertThat(modulesByArea).containsEntry("FINANCE",
                List.of("FINANCE_INVOICES", "FINANCE_RETURNS", "FINANCE_CREDIT_NOTES",
                        "FINANCE_DEBIT_NOTES", "FINANCE_CASH_REGISTER",
                        "FINANCE_RECEIVABLES", "FINANCE_PAYMENT_METHODS"));
        assertThat(modulesByArea).containsEntry("CONTACTS",
                List.of("CONTACTS_CUSTOMERS", "CONTACTS_SUPPLIERS"));
        assertThat(modulesByArea).containsEntry("ANALYTICS",
                List.of("DASHBOARD"));
        assertThat(modulesByArea).containsEntry("PLATFORM",
                List.of("USERS", "ROLES", "DEPARTMENTS"));
    }

    @Test
    void orders_areas_domains_and_permissions_deterministically() {
        var areas = PermissionCatalog.areas();

        assertThat(areas).isSortedAccordingTo(
                Comparator.comparingInt(PermissionCatalog.AreaCatalog::sortOrder)
                        .thenComparing(
                                PermissionCatalog.AreaCatalog::area));
        for (var area : areas) {
            assertThat(area.domains()).isSortedAccordingTo(
                    Comparator
                            .comparingInt(
                                    PermissionCatalog.DomainCatalog::sortOrder)
                            .thenComparing(
                                    domain -> domain.module().name()));
            area.domains().forEach(domain -> assertThat(domain.permissions())
                    .isSortedAccordingTo(Comparator
                            .comparingInt(
                                    PermissionCatalog.PermissionEntry::sortOrder)
                            .thenComparing(entry -> entry.action().name())));
        }
    }

    @Test
    void matches_the_agreed_contract_for_finance_invoices() {
        var finance = areaOf("FINANCE");
        var invoices = domainOf(BusinessModule.FINANCE_INVOICES);

        assertThat(finance.sortOrder()).isEqualTo(50);
        assertThat(invoices.sortOrder()).isEqualTo(10);
        assertThat(invoices.labelKey())
                .isEqualTo("platform.auth.domains.finance_invoices");
        var create = invoices.permissions().stream()
                .filter(entry -> entry.action() == PermissionAction.CREATE)
                .findFirst().orElseThrow();
        assertThat(create.labelKey()).isEqualTo(
                "platform.auth.permissions.finance_invoices.create");
        assertThat(create.descriptionKey()).isEqualTo(
                "platform.auth.permissions.finance_invoices.create.description");
        assertThat(create.sortOrder()).isEqualTo(20);
    }

    @Test
    void exposes_area_label_keys_for_every_area() {
        PermissionCatalog.areas().forEach(area -> assertThat(area.labelKey())
                .isEqualTo("platform.auth.areas."
                        + area.area().toLowerCase()));
    }

    @Test
    void groups_customers_suppliers_and_customer_balance_under_contacts() {
        var contacts = areaOf("CONTACTS");
        assertThat(contacts.domains()).extracting(PermissionCatalog.DomainCatalog::module)
                .containsExactly(BusinessModule.CONTACTS_CUSTOMERS,
                        BusinessModule.CONTACTS_SUPPLIERS);
        assertThat(domainOf(BusinessModule.CONTACTS_CUSTOMERS).labelKey())
                .isEqualTo("platform.auth.domains.customers");
        assertThat(domainOf(BusinessModule.CONTACTS_SUPPLIERS).labelKey())
                .isEqualTo("platform.auth.domains.suppliers");
    }

    @Test
    void customer_balance_uses_the_customers_read_permission() {
        var balance = domainOf(BusinessModule.CONTACTS_CUSTOMERS);

        assertThat(balance.labelKey())
                .isEqualTo("platform.auth.domains.customers");
        assertThat(balance.permissions())
                .extracting(PermissionCatalog.PermissionEntry::action)
                .contains(PermissionAction.READ);
        assertThat(balance.permissions().get(0).labelKey())
                .isEqualTo("platform.auth.permissions.customers.read");
        assertThat(areaOfDomain(BusinessModule.CONTACTS_CUSTOMERS).area())
                .isEqualTo("CONTACTS");
    }

    @Test
    void keeps_receivables_under_finance_treasury_not_under_contacts() {
        assertThat(areaOfDomain(BusinessModule.FINANCE_RECEIVABLES).area())
                .isEqualTo("FINANCE");
    }

    @Test
    void does_not_expose_documents_or_company_anywhere_in_the_catalog() {
        var names = PermissionCatalog.areas().stream()
                .flatMap(area -> area.domains().stream())
                .map(domain -> domain.module().name()).toList();

        assertThat(names).doesNotContain("DOCUMENTS", "COMPANY",
                "FINANCE_BILLING", "FINANCE_TREASURY");
        assertThat(PermissionCatalog.areas()).extracting(
                PermissionCatalog.AreaCatalog::area)
                .doesNotContain("DOCUMENTS");
    }

    @Test
    void navbar_tree_fixes_the_complete_permission_contract() throws Exception {
        assertThat(flattenNavbarTree()).containsExactly(
                "Ventas / Presupuestos / Ver presupuestos = SALES_QUOTES:READ",
                "Ventas / Presupuestos / Ver detalle de presupuesto = SALES_QUOTES:READ",
                "Ventas / Presupuestos / Editar presupuesto = SALES_QUOTES:UPDATE",
                "Ventas / Presupuestos / Crear presupuesto = SALES_QUOTES:CREATE",
                "Ventas / Presupuestos / Descargar PDF de presupuesto = SALES_QUOTES:EXPORT",
                "Ventas / Presupuestos / Convertir a pedido = SALES_ORDERS:CREATE",
                "Ventas / Presupuestos / Convertir a factura = FINANCE_INVOICES:CREATE",
                "Ventas / Presupuestos / Reporte de Presupuestos PDF = SALES_QUOTES:EXPORT",
                "Ventas / Pedidos / Ver pedidos = SALES_ORDERS:READ",
                "Ventas / Pedidos / Ver detalle de pedido = SALES_ORDERS:READ",
                "Ventas / Pedidos / Editar pedido = SALES_ORDERS:UPDATE",
                "Ventas / Pedidos / Crear pedido = SALES_ORDERS:CREATE",
                "Ventas / Pedidos / Descargar PDF de pedido = SALES_ORDERS:EXPORT",
                "Ventas / Pedidos / Confirmar pedido = SALES_ORDERS:CONFIRM",
                "Ventas / Pedidos / Convertir a factura = FINANCE_INVOICES:CREATE",
                "Ventas / Pedidos / Cancelar pedido = SALES_ORDERS:CANCEL",
                "Ventas / Pedidos / Reporte de Pedidos PDF = SALES_ORDERS:EXPORT",
                "Compras / Compras / Ver compras = PURCHASES:READ",
                "Compras / Compras / Ver detalle de compra = PURCHASES:READ",
                "Compras / Compras / Editar compra = PURCHASES:UPDATE",
                "Compras / Compras / Crear compra = PURCHASES:CREATE",
                "Compras / Compras / Descargar PDF de compra = PURCHASES:EXPORT",
                "Compras / Compras / Confirmar compra = PURCHASES:CONFIRM",
                "Compras / Compras / Cancelar compra = PURCHASES:CANCEL",
                "Compras / Compras / Reporte de Compras PDF = PURCHASES:EXPORT",
                "Catálogo / Productos / Ver productos = CATALOG_PRODUCTS:READ",
                "Catálogo / Productos / Ver detalle de producto = CATALOG_PRODUCTS:READ",
                "Catálogo / Productos / Editar producto = CATALOG_PRODUCTS:UPDATE",
                "Catálogo / Productos / Crear producto = CATALOG_PRODUCTS:CREATE",
                "Catálogo / Productos / Reporte de Productos PDF = CATALOG_PRODUCTS:EXPORT",
                "Catálogo / Productos / Catálogo Comercial PDF = CATALOG_PRODUCTS:EXPORT",
                "Catálogo / Categorías / Ver categorías = CATALOG_CATEGORIES:READ",
                "Catálogo / Categorías / Ver detalle de categoría = CATALOG_CATEGORIES:READ",
                "Catálogo / Categorías / Editar categoría = CATALOG_CATEGORIES:UPDATE",
                "Catálogo / Categorías / Crear categoría = CATALOG_CATEGORIES:CREATE",
                "Catálogo / Tipos de precios / Ver tipos de precios = CATALOG_PRICES:READ",
                "Catálogo / Tipos de precios / Ver detalle de tipo de precio = CATALOG_PRICES:READ",
                "Catálogo / Tipos de precios / Editar tipo de precio = CATALOG_PRICES:UPDATE",
                "Catálogo / Tipos de precios / Crear tipo de precio = CATALOG_PRICES:CREATE",
                "Inventario / Almacenes / Ver almacenes = INVENTORY_WAREHOUSES:READ",
                "Inventario / Almacenes / Ver detalle de almacén = INVENTORY_WAREHOUSES:READ",
                "Inventario / Almacenes / Editar almacén = INVENTORY_WAREHOUSES:UPDATE",
                "Inventario / Almacenes / Crear almacén = INVENTORY_WAREHOUSES:CREATE",
                "Finanzas / Facturación / Facturas / Ver facturas = FINANCE_INVOICES:READ",
                "Finanzas / Facturación / Facturas / Ver detalle de factura = FINANCE_INVOICES:READ",
                "Finanzas / Facturación / Facturas / Editar factura = FINANCE_INVOICES:UPDATE",
                "Finanzas / Facturación / Facturas / Crear factura = FINANCE_INVOICES:CREATE",
                "Finanzas / Facturación / Facturas / Descargar PDF de factura = FINANCE_INVOICES:EXPORT",
                "Finanzas / Facturación / Facturas / Confirmar factura = FINANCE_INVOICES:CONFIRM",
                "Finanzas / Facturación / Facturas / Cancelar factura = FINANCE_INVOICES:CANCEL",
                "Finanzas / Facturación / Facturas / Duplicar factura = FINANCE_INVOICES:DUPLICATE",
                "Finanzas / Facturación / Facturas / Reporte de Facturas PDF = FINANCE_INVOICES:EXPORT",
                "Finanzas / Facturación / Devoluciones / Ver devoluciones = FINANCE_RETURNS:READ",
                "Finanzas / Facturación / Devoluciones / Ver detalle de devolución = FINANCE_RETURNS:READ",
                "Finanzas / Facturación / Devoluciones / Crear devolución = FINANCE_RETURNS:CREATE",
                "Finanzas / Facturación / Notas de crédito / Ver notas de crédito = FINANCE_CREDIT_NOTES:READ",
                "Finanzas / Facturación / Notas de crédito / Ver detalle de nota de crédito = FINANCE_CREDIT_NOTES:READ",
                "Finanzas / Facturación / Notas de crédito / Crear nota de crédito = FINANCE_CREDIT_NOTES:CREATE",
                "Finanzas / Facturación / Notas de débito / Ver notas de débito = FINANCE_DEBIT_NOTES:READ",
                "Finanzas / Facturación / Notas de débito / Ver detalle de nota de débito = FINANCE_DEBIT_NOTES:READ",
                "Finanzas / Facturación / Notas de débito / Crear nota de débito = FINANCE_DEBIT_NOTES:CREATE",
                "Finanzas / Tesorería / Caja / Ver caja = FINANCE_CASH_REGISTER:READ",
                "Finanzas / Tesorería / Caja / Ver detalles = FINANCE_CASH_REGISTER:READ",
                "Finanzas / Tesorería / Caja / Abrir caja = FINANCE_CASH_REGISTER:EXECUTE",
                "Finanzas / Tesorería / Caja / Cerrar caja = FINANCE_CASH_REGISTER:EXECUTE",
                "Finanzas / Tesorería / Caja / Reportes de Caja = FINANCE_CASH_REGISTER:EXPORT",
                "Finanzas / Tesorería / Cuentas por cobrar / Ver cuentas por cobrar = FINANCE_RECEIVABLES:READ",
                "Finanzas / Tesorería / Cuentas por cobrar / Ver deuda = FINANCE_RECEIVABLES:DETAILS",
                "Finanzas / Tesorería / Cuentas por cobrar / Pagar todo = FINANCE_RECEIVABLES:PAY_ALL",
                "Finanzas / Tesorería / Cuentas por cobrar / Pago parcial = FINANCE_RECEIVABLES:PARTIAL_PAYMENT",
                "Finanzas / Tesorería / Cuentas por cobrar / Reintegro = FINANCE_RECEIVABLES:REFUND",
                "Finanzas / Tesorería / Cuentas por cobrar / Generar anticipo = FINANCE_RECEIVABLES:ADVANCE",
                "Finanzas / Tesorería / Cuentas por cobrar / Aprobar pago = FINANCE_RECEIVABLES:APPROVE_PAYMENT",
                "Finanzas / Tesorería / Cuentas por cobrar / Rechazar pago = FINANCE_RECEIVABLES:REJECT_PAYMENT",
                "Finanzas / Tesorería / Cuentas por cobrar / Cuentas por Cobrar PDF = FINANCE_RECEIVABLES:REPORT",
                "Finanzas / Tesorería / Métodos de pago / Ver métodos de pago = FINANCE_PAYMENT_METHODS:READ",
                "Finanzas / Tesorería / Métodos de pago / Ver detalle de método de pago = FINANCE_PAYMENT_METHODS:READ",
                "Finanzas / Tesorería / Métodos de pago / Editar método de pago = FINANCE_PAYMENT_METHODS:UPDATE",
                "Finanzas / Tesorería / Métodos de pago / Crear método de pago = FINANCE_PAYMENT_METHODS:CREATE",
                "Contactos / Clientes / Ver clientes = CONTACTS_CUSTOMERS:READ",
                "Contactos / Clientes / Ver detalle de cliente = CONTACTS_CUSTOMERS:READ",
                "Contactos / Clientes / Editar cliente = CONTACTS_CUSTOMERS:UPDATE",
                "Contactos / Clientes / Crear cliente = CONTACTS_CUSTOMERS:CREATE",
                "Contactos / Clientes / Ver saldo de cliente = CONTACTS_CUSTOMERS:READ",
                "Contactos / Proveedores / Ver proveedores = CONTACTS_SUPPLIERS:READ",
                "Contactos / Proveedores / Ver detalle de proveedor = CONTACTS_SUPPLIERS:READ",
                "Contactos / Proveedores / Editar proveedor = CONTACTS_SUPPLIERS:UPDATE",
                "Contactos / Proveedores / Crear proveedor = CONTACTS_SUPPLIERS:CREATE",
                "Análisis / Dashboard = DASHBOARD:READ",
                "Plataforma / Usuarios / Ver usuarios = USERS:READ",
                "Plataforma / Usuarios / Ver detalle de usuario = USERS:READ",
                "Plataforma / Usuarios / Editar usuario = USERS:UPDATE",
                "Plataforma / Usuarios / Crear usuario = USERS:CREATE",
                "Plataforma / Roles / Ver roles = ROLES:READ",
                "Plataforma / Roles / Ver detalle de rol = ROLES:READ",
                "Plataforma / Roles / Editar rol = ROLES:UPDATE",
                "Plataforma / Roles / Crear rol = ROLES:CREATE",
                "Plataforma / Departamentos / Ver departamentos = DEPARTMENTS:READ",
                "Plataforma / Departamentos / Ver detalle de departamento = DEPARTMENTS:READ",
                "Plataforma / Departamentos / Editar departamento = DEPARTMENTS:UPDATE",
                "Plataforma / Departamentos / Crear departamento = DEPARTMENTS:CREATE");
     }

    @Test
    void every_business_module_is_documented_in_the_navbar_contract() throws Exception {
        var modules = mapper().readTree(navbarTree()).get("modules");

        assertThat(modules).isNotNull();
        for (var module : BusinessModule.values()) {
            assertThat(modules.has(module.name()))
                    .as("documented module %s", module.name()).isTrue();
        }
        assertThat(modules.size()).isEqualTo(BusinessModule.values().length);
    }

    @Test
    void navbar_tree_areas_follow_the_catalog_order() throws Exception {
        var areas = mapper().readTree(navbarTree()).get("areas");

        assertThat(areas).hasSize(PermissionCatalog.areas().size());
        for (int index = 0; index < areas.size(); index++) {
            assertThat(areas.get(index).get("area").asText())
                    .isEqualTo(PermissionCatalog.areas().get(index).area());
        }
    }

    @Test
    void rejects_a_registered_pair_without_a_catalog_entry() {
        var invoices = domainOf(BusinessModule.FINANCE_INVOICES);
        var permissions = invoices.permissions().stream()
                .filter(entry -> entry.action() != PermissionAction.CANCEL)
                .toList();

        assertThatRejected(
                replaceDomain(BusinessModule.FINANCE_INVOICES, permissions),
                "Registry permission without catalog entry: FINANCE_INVOICES:CANCEL");
    }

    @Test
    void rejects_a_catalog_entry_that_is_not_registered() {
        var users = domainOf(BusinessModule.USERS);
        var permissions = new ArrayList<>(users.permissions());
        permissions.add(new PermissionCatalog.PermissionEntry(
                PermissionAction.IMPORT,
                "platform.auth.permissions.users.import",
                "platform.auth.permissions.users.import.description",
                TEST_ORDER));

        assertThatRejected(replaceDomain(BusinessModule.USERS, permissions),
                "Catalog permission is not registered: USERS:IMPORT");
    }

    @Test
    void rejects_a_pair_classified_more_than_once() {
        var invoices = domainOf(BusinessModule.FINANCE_INVOICES);
        var permissions = new ArrayList<>(invoices.permissions());
        permissions.add(new PermissionCatalog.PermissionEntry(
                PermissionAction.CREATE,
                "platform.auth.permissions.finance_invoices.create",
                "platform.auth.permissions.finance_invoices.create.description",
                TEST_ORDER));

        assertThatRejected(
                replaceDomain(BusinessModule.FINANCE_INVOICES, permissions),
                "Permission classified twice: FINANCE_INVOICES:CREATE");
    }

    @Test
    void rejects_an_area_declared_more_than_once() {
        var areas = new ArrayList<>(PermissionCatalog.areas());
        areas.add(areaOf("ANALYTICS"));

        assertThatRejected(areas, "Area declared twice: ANALYTICS");
    }

    private static void assertThatRejected(
            List<PermissionCatalog.AreaCatalog> areas, String message) {
        assertThatThrownBy(() -> validate().invoke(null, areas))
                .isInstanceOf(InvocationTargetException.class).cause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(message);
    }

    private static List<PermissionCatalog.AreaCatalog> replaceDomain(
            BusinessModule module,
            List<PermissionCatalog.PermissionEntry> permissions) {
        var current = domainOf(module);
        var replacement = new PermissionCatalog.DomainCatalog(module,
                current.labelKey(), current.sortOrder(), permissions);
        return PermissionCatalog.areas().stream()
                .map(area -> new PermissionCatalog.AreaCatalog(area.area(),
                        area.labelKey(), area.sortOrder(),
                        area.domains().stream()
                                .map(domain -> domain.module() == module
                                        ? replacement
                                        : domain)
                                .toList()))
                .toList();
    }

    private static PermissionCatalog.AreaCatalog areaOf(String area) {
        return PermissionCatalog.areas().stream()
                .filter(catalog -> catalog.area().equals(area)).findFirst()
                .orElseThrow();
    }

    private static PermissionCatalog.AreaCatalog areaOfDomain(
            BusinessModule module) {
        return PermissionCatalog.areas().stream()
                .filter(area -> area.domains().stream()
                        .anyMatch(domain -> domain.module() == module))
                .findFirst().orElseThrow();
    }

    private static PermissionCatalog.DomainCatalog domainOf(
            BusinessModule module) {
        return PermissionCatalog.areas().stream()
                .flatMap(area -> area.domains().stream())
                .filter(domain -> domain.module() == module).findFirst()
                .orElseThrow();
    }

    private static EnumMap<BusinessModule, Set<PermissionAction>> classifiedActions() {
        var classified = new EnumMap<BusinessModule, Set<PermissionAction>>(
                BusinessModule.class);
        for (var area : PermissionCatalog.areas()) {
            for (var domain : area.domains()) {
                var actions = classified.computeIfAbsent(domain.module(),
                        key -> EnumSet.noneOf(PermissionAction.class));
                for (var entry : domain.permissions()) {
                    assertThat(actions.add(entry.action()))
                            .as("%s:%s", domain.module().name(),
                                    entry.action().name())
                            .isTrue();
                }
            }
        }
        return classified;
    }

    private static List<String> flattenNavbarTree() throws Exception {
        var lines = new ArrayList<String>();
        for (var area : mapper().readTree(navbarTree()).get("areas")) {
            flatten(area, "", lines);
        }
        return lines;
    }

    private static void flatten(JsonNode node, String prefix,
            List<String> lines) {
        var path = prefix.isEmpty()
                ? node.get("label").asText()
                : prefix + " / " + node.get("label").asText();
        if (node.has("permission")) {
            lines.add(path + " = " + node.get("permission").asText());
        }
        if (node.has("children")) {
            for (var child : node.get("children")) {
                flatten(child, path, lines);
            }
        }
    }

    private static String navbarTree() throws Exception {
        try (var stream = PermissionCatalogTest.class
                .getResourceAsStream(NAVBAR_TREE)) {
            assertThat(stream).as(NAVBAR_TREE).isNotNull();
            return new String(stream.readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static ObjectMapper mapper() {
        return new ObjectMapper();
    }

    private static Method validate() {
        try {
            var method = PermissionCatalog.class.getDeclaredMethod("validate",
                    List.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
