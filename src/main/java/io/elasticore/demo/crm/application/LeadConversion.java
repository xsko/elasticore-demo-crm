package io.elasticore.demo.crm.application;

import io.elasticore.demo.crm.application.dto.AccountDTO;
import io.elasticore.demo.crm.application.dto.ContactDTO;
import io.elasticore.demo.crm.application.dto.LeadDTO;
import io.elasticore.demo.crm.application.dto.OpportunityDTO;
import io.elasticore.demo.crm.application.port.in.AccountUseCase;
import io.elasticore.demo.crm.application.port.in.ContactUseCase;
import io.elasticore.demo.crm.application.port.in.LeadUseCase;
import io.elasticore.demo.crm.application.port.in.OpportunityUseCase;
import io.elasticore.demo.crm.domain.event.LeadConvertedEvent;
import io.elasticore.demo.crm.domain.model.AccountTier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * What converting a lead means: the company becomes an account, the person a contact on
 * it, and the interest an opportunity in its first stage - all owned by the lead's owner.
 *
 * <p>The lifecycle ({@code convert: QUALIFIED -> CONVERTED}) is the DSL's; it publishes
 * {@link LeadConvertedEvent}, and this listener does the rest in the same transaction, as
 * the signed-in member, so the generated access checks hold for every row it writes.
 */
@Component
class LeadConversion {

    private final LeadUseCase leads;
    private final AccountUseCase accounts;
    private final ContactUseCase contacts;
    private final OpportunityUseCase opportunities;
    /** The deal a conversion opens is named in the language the data is kept in (crm.seed.language). */
    private final String dealName;

    LeadConversion(LeadUseCase leads, AccountUseCase accounts, ContactUseCase contacts, OpportunityUseCase opportunities,
                   @Value("${crm.seed.language:en}") String dataLanguage) {
        this.leads = leads;
        this.accounts = accounts;
        this.contacts = contacts;
        this.opportunities = opportunities;
        this.dealName = dataLanguage != null && dataLanguage.trim().toLowerCase().startsWith("ko") ? "%s 신규 도입" : "%s - New business";
    }

    @EventListener
    void converted(LeadConvertedEvent event) {
        LeadDTO lead = leads.findById(event.getLeadId()).orElse(null);
        if (lead == null) return;

        AccountDTO a = new AccountDTO();
        a.setName(lead.getCompany());
        a.setIndustry(lead.getIndustry());
        a.setTier(AccountTier.STANDARD);
        a.setOwnerMemberId(lead.getOwnerMemberId());
        a.setNote(lead.getNote());
        AccountDTO account = accounts.save(a);

        ContactDTO c = new ContactDTO();
        c.setName(lead.getContactName());
        c.setEmail(lead.getEmail());
        c.setPhone(lead.getPhone());
        c.setAccountId(account.getAccountId());
        c.setOwnerMemberId(lead.getOwnerMemberId());
        contacts.save(c);

        OpportunityDTO o = new OpportunityDTO();
        o.setName(String.format(dealName, lead.getCompany()));
        o.setAccountId(account.getAccountId());
        o.setAmount(lead.getEstimatedValue());
        o.setProbability(20);
        o.setExpectedCloseDate(LocalDate.now().plusDays(60));
        o.setOwnerMemberId(lead.getOwnerMemberId());
        opportunities.save(o);
    }
}
