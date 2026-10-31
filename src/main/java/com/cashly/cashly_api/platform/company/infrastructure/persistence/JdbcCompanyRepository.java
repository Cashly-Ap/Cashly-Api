package com.cashly.cashly_api.platform.company.infrastructure.persistence;
import com.cashly.cashly_api.platform.company.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class JdbcCompanyRepository implements CompanyRepository {
    private final JdbcTemplate jdbc;
    public JdbcCompanyRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public Company find() { return query(""); }
    @Override public Company lock() { return query(" for update"); }
    @Override public void createIfAbsent(Company c) {
        var a = c.address();
        jdbc.update("insert ignore into companies(id,singleton_key,legal_name,trade_name,tax_id,email,phone,website,street,city,region,country,postal_code,logo_path,pdf_logo_path) values(?,1,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            bytes(c.id()),c.legalName(),c.tradeName(),c.taxId(),c.email(),c.phone(),c.website(),a.street(),a.city(),a.region(),a.country(),a.postalCode(),c.logoPath(),c.pdfLogoPath());
    }
    private static byte[] bytes(java.util.UUID id) {
        var buffer = java.nio.ByteBuffer.allocate(16);
        buffer.putLong(id.getMostSignificantBits());
        buffer.putLong(id.getLeastSignificantBits());
        return buffer.array();
    }
    private Company query(String suffix) {
        return jdbc.queryForObject("select * from companies where singleton_key=1" + suffix, (row, n) ->
            new Company(Company.INSTALLATION_ID, row.getString("legal_name"), row.getString("trade_name"), row.getString("tax_id"),
                row.getString("email"), row.getString("phone"), row.getString("website"),
                new Company.Address(row.getString("street"),row.getString("city"),row.getString("region"),row.getString("country"),row.getString("postal_code")), row.getString("logo_path"), row.getString("pdf_logo_path")));
    }
    @Override public void save(Company c) {
        var a = c.address();
        jdbc.update("update companies set legal_name=?,trade_name=?,tax_id=?,email=?,phone=?,website=?,street=?,city=?,region=?,country=?,postal_code=?,logo_path=?,pdf_logo_path=? where singleton_key=1",
            c.legalName(),c.tradeName(),c.taxId(),c.email(),c.phone(),c.website(),a.street(),a.city(),a.region(),a.country(),a.postalCode(),c.logoPath(),c.pdfLogoPath());
    }
}
