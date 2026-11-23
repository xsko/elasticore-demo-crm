package io.elasticore.demo.crm.domain.model;


import io.elasticore.springboot3.entity.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;



/**
 * This class serves as a domain-specific unified metadata holder,
 * containing field names and type information for entities.
 *
 * Generated and managed by ElastiCORE.
 */

public class Q {

	public static $Product<Product> Product=new $Product<>();
	public static class $Product<T> {
	
	    private FieldInfo p=null;
	    public $Product() {}
	    public $Product(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Product> joinSpec() {
	    	return new JoinSpec<>(Product.class);
	    }
	    public static UpdateSpec<Product> updateSpec() {
	    	return new UpdateSpec<>(Product.class);
	    }
	    public static DeleteSpec<Product> deleteSpec() {
	    	return new DeleteSpec<>(Product.class);
	    }
	
	    public final FieldInfo<T> getProductId() {return new FieldInfo(Product.class,"productId",Long.class, null,p);}
	    public final String productId="productId";
	    public Specification<T> productId(Op op,Object value) {return getProductId().where(op,value);}
	    public Specification<T> productId(Op op,Object value,boolean chkEmpty) {return getProductId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCode() {return new FieldInfo(Product.class,"code",String.class, null,p);}
	    public final String code="code";
	    public Specification<T> code(Op op,Object value) {return getCode().where(op,value);}
	    public Specification<T> code(Op op,Object value,boolean chkEmpty) {return getCode().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Product.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCategory() {return new FieldInfo(Product.class,"category",ProductCategory.class, null,p);}
	    public final String category="category";
	    public Specification<T> category(Op op,Object value) {return getCategory().where(op,value);}
	    public Specification<T> category(Op op,Object value,boolean chkEmpty) {return getCategory().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPrice() {return new FieldInfo(Product.class,"price",io.elasticore.springboot3.domain.Money.class, null,p);}
	    public final String price="price";
	    public Specification<T> price(Op op,Object value) {return getPrice().where(op,value);}
	    public Specification<T> price(Op op,Object value,boolean chkEmpty) {return getPrice().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getActive() {return new FieldInfo(Product.class,"active",Boolean.class, null,p);}
	    public final String active="active";
	    public Specification<T> active(Op op,Object value) {return getActive().where(op,value);}
	    public Specification<T> active(Op op,Object value,boolean chkEmpty) {return getActive().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDescription() {return new FieldInfo(Product.class,"description",String.class, null,p);}
	    public final String description="description";
	    public Specification<T> description(Op op,Object value) {return getDescription().where(op,value);}
	    public Specification<T> description(Op op,Object value,boolean chkEmpty) {return getDescription().where(op,value,chkEmpty);}
	}
	
	public static $Quote<Quote> Quote=new $Quote<>();
	public static class $Quote<T> {
	
	    private FieldInfo p=null;
	    public $Quote() {}
	    public $Quote(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Quote> joinSpec() {
	    	return new JoinSpec<>(Quote.class);
	    }
	    public static UpdateSpec<Quote> updateSpec() {
	    	return new UpdateSpec<>(Quote.class);
	    }
	    public static DeleteSpec<Quote> deleteSpec() {
	    	return new DeleteSpec<>(Quote.class);
	    }
	
	    public final FieldInfo<T> getQuoteId() {return new FieldInfo(Quote.class,"quoteId",Long.class, null,p);}
	    public final String quoteId="quoteId";
	    public Specification<T> quoteId(Op op,Object value) {return getQuoteId().where(op,value);}
	    public Specification<T> quoteId(Op op,Object value,boolean chkEmpty) {return getQuoteId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTitle() {return new FieldInfo(Quote.class,"title",String.class, null,p);}
	    public final String title="title";
	    public Specification<T> title(Op op,Object value) {return getTitle().where(op,value);}
	    public Specification<T> title(Op op,Object value,boolean chkEmpty) {return getTitle().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOpportunity() {return new FieldInfo(Quote.class,"opportunity",Opportunity.class, null,p);}
	    public final String opportunity="opportunity";
	    public final $Opportunity<T> opportunity() {return new $Opportunity(getOpportunity());}
	
	    public final FieldInfo<T> getValidUntil() {return new FieldInfo(Quote.class,"validUntil",java.time.LocalDate.class, null,p);}
	    public final String validUntil="validUntil";
	    public Specification<T> validUntil(Op op,Object value) {return getValidUntil().where(op,value);}
	    public Specification<T> validUntil(Op op,Object value,boolean chkEmpty) {return getValidUntil().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStatus() {return new FieldInfo(Quote.class,"status",QuoteStatus.class, null,p);}
	    public final String status="status";
	    public Specification<T> status(Op op,Object value) {return getStatus().where(op,value);}
	    public Specification<T> status(Op op,Object value,boolean chkEmpty) {return getStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTotal() {return new FieldInfo(Quote.class,"total",io.elasticore.springboot3.domain.Money.class, null,p);}
	    public final String total="total";
	    public Specification<T> total(Op op,Object value) {return getTotal().where(op,value);}
	    public Specification<T> total(Op op,Object value,boolean chkEmpty) {return getTotal().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLines() {return new FieldInfo(Quote.class,"lines",QuoteLine.class, List.class,p);}
	    public final String lines="lines";
	
	    public final FieldInfo<T> getOwner() {return new FieldInfo(Quote.class,"owner",Member.class, null,p);}
	    public final String owner="owner";
	    public final $Member<T> owner() {return new $Member(getOwner());}
	
	    public final FieldInfo<T> getCreatedAt() {return new FieldInfo(Quote.class,"createdAt",java.time.LocalDateTime.class, null,p);}
	    public final String createdAt="createdAt";
	    public Specification<T> createdAt(Op op,Object value) {return getCreatedAt().where(op,value);}
	    public Specification<T> createdAt(Op op,Object value,boolean chkEmpty) {return getCreatedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSentAt() {return new FieldInfo(Quote.class,"sentAt",java.time.Instant.class, null,p);}
	    public final String sentAt="sentAt";
	    public Specification<T> sentAt(Op op,Object value) {return getSentAt().where(op,value);}
	    public Specification<T> sentAt(Op op,Object value,boolean chkEmpty) {return getSentAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOpportunityId() {return new FieldInfo(Quote.class,"opportunityId",Long.class, null,p);}
	    public final String opportunityId="opportunityId";
	    public Specification<T> opportunityId(Op op,Object value) {return getOpportunityId().where(op,value);}
	    public Specification<T> opportunityId(Op op,Object value,boolean chkEmpty) {return getOpportunityId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwnerMemberId() {return new FieldInfo(Quote.class,"ownerMemberId",Long.class, null,p);}
	    public final String ownerMemberId="ownerMemberId";
	    public Specification<T> ownerMemberId(Op op,Object value) {return getOwnerMemberId().where(op,value);}
	    public Specification<T> ownerMemberId(Op op,Object value,boolean chkEmpty) {return getOwnerMemberId().where(op,value,chkEmpty);}
	}
	
	public static $QuoteLine<QuoteLine> QuoteLine=new $QuoteLine<>();
	public static class $QuoteLine<T> {
	
	    private FieldInfo p=null;
	    public $QuoteLine() {}
	    public $QuoteLine(FieldInfo p) {this.p=p;}
	    public static JoinSpec<QuoteLine> joinSpec() {
	    	return new JoinSpec<>(QuoteLine.class);
	    }
	    public static UpdateSpec<QuoteLine> updateSpec() {
	    	return new UpdateSpec<>(QuoteLine.class);
	    }
	    public static DeleteSpec<QuoteLine> deleteSpec() {
	    	return new DeleteSpec<>(QuoteLine.class);
	    }
	
	    public final FieldInfo<T> getLineId() {return new FieldInfo(QuoteLine.class,"lineId",Long.class, null,p);}
	    public final String lineId="lineId";
	    public Specification<T> lineId(Op op,Object value) {return getLineId().where(op,value);}
	    public Specification<T> lineId(Op op,Object value,boolean chkEmpty) {return getLineId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getQuote() {return new FieldInfo(QuoteLine.class,"quote",Quote.class, null,p);}
	    public final String quote="quote";
	    public final $Quote<T> quote() {return new $Quote(getQuote());}
	
	    public final FieldInfo<T> getProduct() {return new FieldInfo(QuoteLine.class,"product",Product.class, null,p);}
	    public final String product="product";
	    public final $Product<T> product() {return new $Product(getProduct());}
	
	    public final FieldInfo<T> getQuantity() {return new FieldInfo(QuoteLine.class,"quantity",io.elasticore.springboot3.domain.Quantity.class, null,p);}
	    public final String quantity="quantity";
	    public Specification<T> quantity(Op op,Object value) {return getQuantity().where(op,value);}
	    public Specification<T> quantity(Op op,Object value,boolean chkEmpty) {return getQuantity().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getUnitPrice() {return new FieldInfo(QuoteLine.class,"unitPrice",io.elasticore.springboot3.domain.Money.class, null,p);}
	    public final String unitPrice="unitPrice";
	    public Specification<T> unitPrice(Op op,Object value) {return getUnitPrice().where(op,value);}
	    public Specification<T> unitPrice(Op op,Object value,boolean chkEmpty) {return getUnitPrice().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSubTotal() {return new FieldInfo(QuoteLine.class,"subTotal",io.elasticore.springboot3.domain.Money.class, null,p);}
	    public final String subTotal="subTotal";
	    public Specification<T> subTotal(Op op,Object value) {return getSubTotal().where(op,value);}
	    public Specification<T> subTotal(Op op,Object value,boolean chkEmpty) {return getSubTotal().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getProductId() {return new FieldInfo(QuoteLine.class,"productId",Long.class, null,p);}
	    public final String productId="productId";
	    public Specification<T> productId(Op op,Object value) {return getProductId().where(op,value);}
	    public Specification<T> productId(Op op,Object value,boolean chkEmpty) {return getProductId().where(op,value,chkEmpty);}
	}
	
	public static $Member<Member> Member=new $Member<>();
	public static class $Member<T> {
	
	    private FieldInfo p=null;
	    public $Member() {}
	    public $Member(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Member> joinSpec() {
	    	return new JoinSpec<>(Member.class);
	    }
	    public static UpdateSpec<Member> updateSpec() {
	    	return new UpdateSpec<>(Member.class);
	    }
	    public static DeleteSpec<Member> deleteSpec() {
	    	return new DeleteSpec<>(Member.class);
	    }
	
	    public final FieldInfo<T> getMemberId() {return new FieldInfo(Member.class,"memberId",Long.class, null,p);}
	    public final String memberId="memberId";
	    public Specification<T> memberId(Op op,Object value) {return getMemberId().where(op,value);}
	    public Specification<T> memberId(Op op,Object value,boolean chkEmpty) {return getMemberId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLogin() {return new FieldInfo(Member.class,"login",String.class, null,p);}
	    public final String login="login";
	    public Specification<T> login(Op op,Object value) {return getLogin().where(op,value);}
	    public Specification<T> login(Op op,Object value,boolean chkEmpty) {return getLogin().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPassword() {return new FieldInfo(Member.class,"password",String.class, null,p);}
	    public final String password="password";
	    public Specification<T> password(Op op,Object value) {return getPassword().where(op,value);}
	    public Specification<T> password(Op op,Object value,boolean chkEmpty) {return getPassword().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Member.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEmail() {return new FieldInfo(Member.class,"email",String.class, null,p);}
	    public final String email="email";
	    public Specification<T> email(Op op,Object value) {return getEmail().where(op,value);}
	    public Specification<T> email(Op op,Object value,boolean chkEmpty) {return getEmail().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPhone() {return new FieldInfo(Member.class,"phone",String.class, null,p);}
	    public final String phone="phone";
	    public Specification<T> phone(Op op,Object value) {return getPhone().where(op,value);}
	    public Specification<T> phone(Op op,Object value,boolean chkEmpty) {return getPhone().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCompany() {return new FieldInfo(Member.class,"company",String.class, null,p);}
	    public final String company="company";
	    public Specification<T> company(Op op,Object value) {return getCompany().where(op,value);}
	    public Specification<T> company(Op op,Object value,boolean chkEmpty) {return getCompany().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getRole() {return new FieldInfo(Member.class,"role",MemberRole.class, null,p);}
	    public final String role="role";
	    public Specification<T> role(Op op,Object value) {return getRole().where(op,value);}
	    public Specification<T> role(Op op,Object value,boolean chkEmpty) {return getRole().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getJoinedAt() {return new FieldInfo(Member.class,"joinedAt",java.time.LocalDateTime.class, null,p);}
	    public final String joinedAt="joinedAt";
	    public Specification<T> joinedAt(Op op,Object value) {return getJoinedAt().where(op,value);}
	    public Specification<T> joinedAt(Op op,Object value,boolean chkEmpty) {return getJoinedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPhoneBidx() {return new FieldInfo(Member.class,"phoneBidx",String.class, null,p);}
	    public final String phoneBidx="phoneBidx";
	    public Specification<T> phoneBidx(Op op,Object value) {return getPhoneBidx().where(op,value);}
	    public Specification<T> phoneBidx(Op op,Object value,boolean chkEmpty) {return getPhoneBidx().where(op,value,chkEmpty);}
	}
	
	public static $Account<Account> Account=new $Account<>();
	public static class $Account<T> {
	
	    private FieldInfo p=null;
	    public $Account() {}
	    public $Account(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Account> joinSpec() {
	    	return new JoinSpec<>(Account.class);
	    }
	    public static UpdateSpec<Account> updateSpec() {
	    	return new UpdateSpec<>(Account.class);
	    }
	    public static DeleteSpec<Account> deleteSpec() {
	    	return new DeleteSpec<>(Account.class);
	    }
	
	    public final FieldInfo<T> getAccountId() {return new FieldInfo(Account.class,"accountId",Long.class, null,p);}
	    public final String accountId="accountId";
	    public Specification<T> accountId(Op op,Object value) {return getAccountId().where(op,value);}
	    public Specification<T> accountId(Op op,Object value,boolean chkEmpty) {return getAccountId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Account.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getIndustry() {return new FieldInfo(Account.class,"industry",Industry.class, null,p);}
	    public final String industry="industry";
	    public Specification<T> industry(Op op,Object value) {return getIndustry().where(op,value);}
	    public Specification<T> industry(Op op,Object value,boolean chkEmpty) {return getIndustry().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTier() {return new FieldInfo(Account.class,"tier",AccountTier.class, null,p);}
	    public final String tier="tier";
	    public Specification<T> tier(Op op,Object value) {return getTier().where(op,value);}
	    public Specification<T> tier(Op op,Object value,boolean chkEmpty) {return getTier().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getWebsite() {return new FieldInfo(Account.class,"website",String.class, null,p);}
	    public final String website="website";
	    public Specification<T> website(Op op,Object value) {return getWebsite().where(op,value);}
	    public Specification<T> website(Op op,Object value,boolean chkEmpty) {return getWebsite().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPhone() {return new FieldInfo(Account.class,"phone",String.class, null,p);}
	    public final String phone="phone";
	    public Specification<T> phone(Op op,Object value) {return getPhone().where(op,value);}
	    public Specification<T> phone(Op op,Object value,boolean chkEmpty) {return getPhone().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCity() {return new FieldInfo(Account.class,"city",String.class, null,p);}
	    public final String city="city";
	    public Specification<T> city(Op op,Object value) {return getCity().where(op,value);}
	    public Specification<T> city(Op op,Object value,boolean chkEmpty) {return getCity().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAnnualRevenue() {return new FieldInfo(Account.class,"annualRevenue",io.elasticore.springboot3.domain.Money.class, null,p);}
	    public final String annualRevenue="annualRevenue";
	    public Specification<T> annualRevenue(Op op,Object value) {return getAnnualRevenue().where(op,value);}
	    public Specification<T> annualRevenue(Op op,Object value,boolean chkEmpty) {return getAnnualRevenue().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEmployees() {return new FieldInfo(Account.class,"employees",Integer.class, null,p);}
	    public final String employees="employees";
	    public Specification<T> employees(Op op,Object value) {return getEmployees().where(op,value);}
	    public Specification<T> employees(Op op,Object value,boolean chkEmpty) {return getEmployees().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getNote() {return new FieldInfo(Account.class,"note",String.class, null,p);}
	    public final String note="note";
	    public Specification<T> note(Op op,Object value) {return getNote().where(op,value);}
	    public Specification<T> note(Op op,Object value,boolean chkEmpty) {return getNote().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwner() {return new FieldInfo(Account.class,"owner",Member.class, null,p);}
	    public final String owner="owner";
	    public final $Member<T> owner() {return new $Member(getOwner());}
	
	    public final FieldInfo<T> getCreatedAt() {return new FieldInfo(Account.class,"createdAt",java.time.LocalDateTime.class, null,p);}
	    public final String createdAt="createdAt";
	    public Specification<T> createdAt(Op op,Object value) {return getCreatedAt().where(op,value);}
	    public Specification<T> createdAt(Op op,Object value,boolean chkEmpty) {return getCreatedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwnerMemberId() {return new FieldInfo(Account.class,"ownerMemberId",Long.class, null,p);}
	    public final String ownerMemberId="ownerMemberId";
	    public Specification<T> ownerMemberId(Op op,Object value) {return getOwnerMemberId().where(op,value);}
	    public Specification<T> ownerMemberId(Op op,Object value,boolean chkEmpty) {return getOwnerMemberId().where(op,value,chkEmpty);}
	}
	
	public static $Contact<Contact> Contact=new $Contact<>();
	public static class $Contact<T> {
	
	    private FieldInfo p=null;
	    public $Contact() {}
	    public $Contact(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Contact> joinSpec() {
	    	return new JoinSpec<>(Contact.class);
	    }
	    public static UpdateSpec<Contact> updateSpec() {
	    	return new UpdateSpec<>(Contact.class);
	    }
	    public static DeleteSpec<Contact> deleteSpec() {
	    	return new DeleteSpec<>(Contact.class);
	    }
	
	    public final FieldInfo<T> getContactId() {return new FieldInfo(Contact.class,"contactId",Long.class, null,p);}
	    public final String contactId="contactId";
	    public Specification<T> contactId(Op op,Object value) {return getContactId().where(op,value);}
	    public Specification<T> contactId(Op op,Object value,boolean chkEmpty) {return getContactId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Contact.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getJobTitle() {return new FieldInfo(Contact.class,"jobTitle",String.class, null,p);}
	    public final String jobTitle="jobTitle";
	    public Specification<T> jobTitle(Op op,Object value) {return getJobTitle().where(op,value);}
	    public Specification<T> jobTitle(Op op,Object value,boolean chkEmpty) {return getJobTitle().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEmail() {return new FieldInfo(Contact.class,"email",String.class, null,p);}
	    public final String email="email";
	    public Specification<T> email(Op op,Object value) {return getEmail().where(op,value);}
	    public Specification<T> email(Op op,Object value,boolean chkEmpty) {return getEmail().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPhone() {return new FieldInfo(Contact.class,"phone",String.class, null,p);}
	    public final String phone="phone";
	    public Specification<T> phone(Op op,Object value) {return getPhone().where(op,value);}
	    public Specification<T> phone(Op op,Object value,boolean chkEmpty) {return getPhone().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAccount() {return new FieldInfo(Contact.class,"account",Account.class, null,p);}
	    public final String account="account";
	    public final $Account<T> account() {return new $Account(getAccount());}
	
	    public final FieldInfo<T> getOwner() {return new FieldInfo(Contact.class,"owner",Member.class, null,p);}
	    public final String owner="owner";
	    public final $Member<T> owner() {return new $Member(getOwner());}
	
	    public final FieldInfo<T> getCreatedAt() {return new FieldInfo(Contact.class,"createdAt",java.time.LocalDateTime.class, null,p);}
	    public final String createdAt="createdAt";
	    public Specification<T> createdAt(Op op,Object value) {return getCreatedAt().where(op,value);}
	    public Specification<T> createdAt(Op op,Object value,boolean chkEmpty) {return getCreatedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAccountId() {return new FieldInfo(Contact.class,"accountId",Long.class, null,p);}
	    public final String accountId="accountId";
	    public Specification<T> accountId(Op op,Object value) {return getAccountId().where(op,value);}
	    public Specification<T> accountId(Op op,Object value,boolean chkEmpty) {return getAccountId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwnerMemberId() {return new FieldInfo(Contact.class,"ownerMemberId",Long.class, null,p);}
	    public final String ownerMemberId="ownerMemberId";
	    public Specification<T> ownerMemberId(Op op,Object value) {return getOwnerMemberId().where(op,value);}
	    public Specification<T> ownerMemberId(Op op,Object value,boolean chkEmpty) {return getOwnerMemberId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEmailBidx() {return new FieldInfo(Contact.class,"emailBidx",String.class, null,p);}
	    public final String emailBidx="emailBidx";
	    public Specification<T> emailBidx(Op op,Object value) {return getEmailBidx().where(op,value);}
	    public Specification<T> emailBidx(Op op,Object value,boolean chkEmpty) {return getEmailBidx().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPhoneBidx() {return new FieldInfo(Contact.class,"phoneBidx",String.class, null,p);}
	    public final String phoneBidx="phoneBidx";
	    public Specification<T> phoneBidx(Op op,Object value) {return getPhoneBidx().where(op,value);}
	    public Specification<T> phoneBidx(Op op,Object value,boolean chkEmpty) {return getPhoneBidx().where(op,value,chkEmpty);}
	}
	
	public static $Lead<Lead> Lead=new $Lead<>();
	public static class $Lead<T> {
	
	    private FieldInfo p=null;
	    public $Lead() {}
	    public $Lead(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Lead> joinSpec() {
	    	return new JoinSpec<>(Lead.class);
	    }
	    public static UpdateSpec<Lead> updateSpec() {
	    	return new UpdateSpec<>(Lead.class);
	    }
	    public static DeleteSpec<Lead> deleteSpec() {
	    	return new DeleteSpec<>(Lead.class);
	    }
	
	    public final FieldInfo<T> getLeadId() {return new FieldInfo(Lead.class,"leadId",Long.class, null,p);}
	    public final String leadId="leadId";
	    public Specification<T> leadId(Op op,Object value) {return getLeadId().where(op,value);}
	    public Specification<T> leadId(Op op,Object value,boolean chkEmpty) {return getLeadId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCompany() {return new FieldInfo(Lead.class,"company",String.class, null,p);}
	    public final String company="company";
	    public Specification<T> company(Op op,Object value) {return getCompany().where(op,value);}
	    public Specification<T> company(Op op,Object value,boolean chkEmpty) {return getCompany().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getContactName() {return new FieldInfo(Lead.class,"contactName",String.class, null,p);}
	    public final String contactName="contactName";
	    public Specification<T> contactName(Op op,Object value) {return getContactName().where(op,value);}
	    public Specification<T> contactName(Op op,Object value,boolean chkEmpty) {return getContactName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEmail() {return new FieldInfo(Lead.class,"email",String.class, null,p);}
	    public final String email="email";
	    public Specification<T> email(Op op,Object value) {return getEmail().where(op,value);}
	    public Specification<T> email(Op op,Object value,boolean chkEmpty) {return getEmail().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPhone() {return new FieldInfo(Lead.class,"phone",String.class, null,p);}
	    public final String phone="phone";
	    public Specification<T> phone(Op op,Object value) {return getPhone().where(op,value);}
	    public Specification<T> phone(Op op,Object value,boolean chkEmpty) {return getPhone().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSource() {return new FieldInfo(Lead.class,"source",LeadSource.class, null,p);}
	    public final String source="source";
	    public Specification<T> source(Op op,Object value) {return getSource().where(op,value);}
	    public Specification<T> source(Op op,Object value,boolean chkEmpty) {return getSource().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getIndustry() {return new FieldInfo(Lead.class,"industry",Industry.class, null,p);}
	    public final String industry="industry";
	    public Specification<T> industry(Op op,Object value) {return getIndustry().where(op,value);}
	    public Specification<T> industry(Op op,Object value,boolean chkEmpty) {return getIndustry().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getEstimatedValue() {return new FieldInfo(Lead.class,"estimatedValue",io.elasticore.springboot3.domain.Money.class, null,p);}
	    public final String estimatedValue="estimatedValue";
	    public Specification<T> estimatedValue(Op op,Object value) {return getEstimatedValue().where(op,value);}
	    public Specification<T> estimatedValue(Op op,Object value,boolean chkEmpty) {return getEstimatedValue().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getNote() {return new FieldInfo(Lead.class,"note",String.class, null,p);}
	    public final String note="note";
	    public Specification<T> note(Op op,Object value) {return getNote().where(op,value);}
	    public Specification<T> note(Op op,Object value,boolean chkEmpty) {return getNote().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStatus() {return new FieldInfo(Lead.class,"status",LeadStatus.class, null,p);}
	    public final String status="status";
	    public Specification<T> status(Op op,Object value) {return getStatus().where(op,value);}
	    public Specification<T> status(Op op,Object value,boolean chkEmpty) {return getStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwner() {return new FieldInfo(Lead.class,"owner",Member.class, null,p);}
	    public final String owner="owner";
	    public final $Member<T> owner() {return new $Member(getOwner());}
	
	    public final FieldInfo<T> getCreatedAt() {return new FieldInfo(Lead.class,"createdAt",java.time.LocalDateTime.class, null,p);}
	    public final String createdAt="createdAt";
	    public Specification<T> createdAt(Op op,Object value) {return getCreatedAt().where(op,value);}
	    public Specification<T> createdAt(Op op,Object value,boolean chkEmpty) {return getCreatedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getConvertedAt() {return new FieldInfo(Lead.class,"convertedAt",java.time.Instant.class, null,p);}
	    public final String convertedAt="convertedAt";
	    public Specification<T> convertedAt(Op op,Object value) {return getConvertedAt().where(op,value);}
	    public Specification<T> convertedAt(Op op,Object value,boolean chkEmpty) {return getConvertedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwnerMemberId() {return new FieldInfo(Lead.class,"ownerMemberId",Long.class, null,p);}
	    public final String ownerMemberId="ownerMemberId";
	    public Specification<T> ownerMemberId(Op op,Object value) {return getOwnerMemberId().where(op,value);}
	    public Specification<T> ownerMemberId(Op op,Object value,boolean chkEmpty) {return getOwnerMemberId().where(op,value,chkEmpty);}
	}
	
	public static $Opportunity<Opportunity> Opportunity=new $Opportunity<>();
	public static class $Opportunity<T> {
	
	    private FieldInfo p=null;
	    public $Opportunity() {}
	    public $Opportunity(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Opportunity> joinSpec() {
	    	return new JoinSpec<>(Opportunity.class);
	    }
	    public static UpdateSpec<Opportunity> updateSpec() {
	    	return new UpdateSpec<>(Opportunity.class);
	    }
	    public static DeleteSpec<Opportunity> deleteSpec() {
	    	return new DeleteSpec<>(Opportunity.class);
	    }
	
	    public final FieldInfo<T> getOpportunityId() {return new FieldInfo(Opportunity.class,"opportunityId",Long.class, null,p);}
	    public final String opportunityId="opportunityId";
	    public Specification<T> opportunityId(Op op,Object value) {return getOpportunityId().where(op,value);}
	    public Specification<T> opportunityId(Op op,Object value,boolean chkEmpty) {return getOpportunityId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getName() {return new FieldInfo(Opportunity.class,"name",String.class, null,p);}
	    public final String name="name";
	    public Specification<T> name(Op op,Object value) {return getName().where(op,value);}
	    public Specification<T> name(Op op,Object value,boolean chkEmpty) {return getName().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAccount() {return new FieldInfo(Opportunity.class,"account",Account.class, null,p);}
	    public final String account="account";
	    public final $Account<T> account() {return new $Account(getAccount());}
	
	    public final FieldInfo<T> getAmount() {return new FieldInfo(Opportunity.class,"amount",io.elasticore.springboot3.domain.Money.class, null,p);}
	    public final String amount="amount";
	    public Specification<T> amount(Op op,Object value) {return getAmount().where(op,value);}
	    public Specification<T> amount(Op op,Object value,boolean chkEmpty) {return getAmount().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getProbability() {return new FieldInfo(Opportunity.class,"probability",Integer.class, null,p);}
	    public final String probability="probability";
	    public Specification<T> probability(Op op,Object value) {return getProbability().where(op,value);}
	    public Specification<T> probability(Op op,Object value,boolean chkEmpty) {return getProbability().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getExpectedCloseDate() {return new FieldInfo(Opportunity.class,"expectedCloseDate",java.time.LocalDate.class, null,p);}
	    public final String expectedCloseDate="expectedCloseDate";
	    public Specification<T> expectedCloseDate(Op op,Object value) {return getExpectedCloseDate().where(op,value);}
	    public Specification<T> expectedCloseDate(Op op,Object value,boolean chkEmpty) {return getExpectedCloseDate().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStage() {return new FieldInfo(Opportunity.class,"stage",OpportunityStage.class, null,p);}
	    public final String stage="stage";
	    public Specification<T> stage(Op op,Object value) {return getStage().where(op,value);}
	    public Specification<T> stage(Op op,Object value,boolean chkEmpty) {return getStage().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getNote() {return new FieldInfo(Opportunity.class,"note",String.class, null,p);}
	    public final String note="note";
	    public Specification<T> note(Op op,Object value) {return getNote().where(op,value);}
	    public Specification<T> note(Op op,Object value,boolean chkEmpty) {return getNote().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwner() {return new FieldInfo(Opportunity.class,"owner",Member.class, null,p);}
	    public final String owner="owner";
	    public final $Member<T> owner() {return new $Member(getOwner());}
	
	    public final FieldInfo<T> getCreatedAt() {return new FieldInfo(Opportunity.class,"createdAt",java.time.LocalDateTime.class, null,p);}
	    public final String createdAt="createdAt";
	    public Specification<T> createdAt(Op op,Object value) {return getCreatedAt().where(op,value);}
	    public Specification<T> createdAt(Op op,Object value,boolean chkEmpty) {return getCreatedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getWonAt() {return new FieldInfo(Opportunity.class,"wonAt",java.time.Instant.class, null,p);}
	    public final String wonAt="wonAt";
	    public Specification<T> wonAt(Op op,Object value) {return getWonAt().where(op,value);}
	    public Specification<T> wonAt(Op op,Object value,boolean chkEmpty) {return getWonAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getLostAt() {return new FieldInfo(Opportunity.class,"lostAt",java.time.Instant.class, null,p);}
	    public final String lostAt="lostAt";
	    public Specification<T> lostAt(Op op,Object value) {return getLostAt().where(op,value);}
	    public Specification<T> lostAt(Op op,Object value,boolean chkEmpty) {return getLostAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAccountId() {return new FieldInfo(Opportunity.class,"accountId",Long.class, null,p);}
	    public final String accountId="accountId";
	    public Specification<T> accountId(Op op,Object value) {return getAccountId().where(op,value);}
	    public Specification<T> accountId(Op op,Object value,boolean chkEmpty) {return getAccountId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwnerMemberId() {return new FieldInfo(Opportunity.class,"ownerMemberId",Long.class, null,p);}
	    public final String ownerMemberId="ownerMemberId";
	    public Specification<T> ownerMemberId(Op op,Object value) {return getOwnerMemberId().where(op,value);}
	    public Specification<T> ownerMemberId(Op op,Object value,boolean chkEmpty) {return getOwnerMemberId().where(op,value,chkEmpty);}
	}
	
	public static $Activity<Activity> Activity=new $Activity<>();
	public static class $Activity<T> {
	
	    private FieldInfo p=null;
	    public $Activity() {}
	    public $Activity(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Activity> joinSpec() {
	    	return new JoinSpec<>(Activity.class);
	    }
	    public static UpdateSpec<Activity> updateSpec() {
	    	return new UpdateSpec<>(Activity.class);
	    }
	    public static DeleteSpec<Activity> deleteSpec() {
	    	return new DeleteSpec<>(Activity.class);
	    }
	
	    public final FieldInfo<T> getActivityId() {return new FieldInfo(Activity.class,"activityId",Long.class, null,p);}
	    public final String activityId="activityId";
	    public Specification<T> activityId(Op op,Object value) {return getActivityId().where(op,value);}
	    public Specification<T> activityId(Op op,Object value,boolean chkEmpty) {return getActivityId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getType() {return new FieldInfo(Activity.class,"type",ActivityType.class, null,p);}
	    public final String type="type";
	    public Specification<T> type(Op op,Object value) {return getType().where(op,value);}
	    public Specification<T> type(Op op,Object value,boolean chkEmpty) {return getType().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSubject() {return new FieldInfo(Activity.class,"subject",String.class, null,p);}
	    public final String subject="subject";
	    public Specification<T> subject(Op op,Object value) {return getSubject().where(op,value);}
	    public Specification<T> subject(Op op,Object value,boolean chkEmpty) {return getSubject().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDueAt() {return new FieldInfo(Activity.class,"dueAt",java.time.LocalDateTime.class, null,p);}
	    public final String dueAt="dueAt";
	    public Specification<T> dueAt(Op op,Object value) {return getDueAt().where(op,value);}
	    public Specification<T> dueAt(Op op,Object value,boolean chkEmpty) {return getDueAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getDone() {return new FieldInfo(Activity.class,"done",Boolean.class, null,p);}
	    public final String done="done";
	    public Specification<T> done(Op op,Object value) {return getDone().where(op,value);}
	    public Specification<T> done(Op op,Object value,boolean chkEmpty) {return getDone().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAccount() {return new FieldInfo(Activity.class,"account",Account.class, null,p);}
	    public final String account="account";
	    public final $Account<T> account() {return new $Account(getAccount());}
	
	    public final FieldInfo<T> getOpportunity() {return new FieldInfo(Activity.class,"opportunity",Opportunity.class, null,p);}
	    public final String opportunity="opportunity";
	    public final $Opportunity<T> opportunity() {return new $Opportunity(getOpportunity());}
	
	    public final FieldInfo<T> getNote() {return new FieldInfo(Activity.class,"note",String.class, null,p);}
	    public final String note="note";
	    public Specification<T> note(Op op,Object value) {return getNote().where(op,value);}
	    public Specification<T> note(Op op,Object value,boolean chkEmpty) {return getNote().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwner() {return new FieldInfo(Activity.class,"owner",Member.class, null,p);}
	    public final String owner="owner";
	    public final $Member<T> owner() {return new $Member(getOwner());}
	
	    public final FieldInfo<T> getAccountId() {return new FieldInfo(Activity.class,"accountId",Long.class, null,p);}
	    public final String accountId="accountId";
	    public Specification<T> accountId(Op op,Object value) {return getAccountId().where(op,value);}
	    public Specification<T> accountId(Op op,Object value,boolean chkEmpty) {return getAccountId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOpportunityId() {return new FieldInfo(Activity.class,"opportunityId",Long.class, null,p);}
	    public final String opportunityId="opportunityId";
	    public Specification<T> opportunityId(Op op,Object value) {return getOpportunityId().where(op,value);}
	    public Specification<T> opportunityId(Op op,Object value,boolean chkEmpty) {return getOpportunityId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getOwnerMemberId() {return new FieldInfo(Activity.class,"ownerMemberId",Long.class, null,p);}
	    public final String ownerMemberId="ownerMemberId";
	    public Specification<T> ownerMemberId(Op op,Object value) {return getOwnerMemberId().where(op,value);}
	    public Specification<T> ownerMemberId(Op op,Object value,boolean chkEmpty) {return getOwnerMemberId().where(op,value,chkEmpty);}
	}
	
	public static $Ticket<Ticket> Ticket=new $Ticket<>();
	public static class $Ticket<T> {
	
	    private FieldInfo p=null;
	    public $Ticket() {}
	    public $Ticket(FieldInfo p) {this.p=p;}
	    public static JoinSpec<Ticket> joinSpec() {
	    	return new JoinSpec<>(Ticket.class);
	    }
	    public static UpdateSpec<Ticket> updateSpec() {
	    	return new UpdateSpec<>(Ticket.class);
	    }
	    public static DeleteSpec<Ticket> deleteSpec() {
	    	return new DeleteSpec<>(Ticket.class);
	    }
	
	    public final FieldInfo<T> getTicketId() {return new FieldInfo(Ticket.class,"ticketId",Long.class, null,p);}
	    public final String ticketId="ticketId";
	    public Specification<T> ticketId(Op op,Object value) {return getTicketId().where(op,value);}
	    public Specification<T> ticketId(Op op,Object value,boolean chkEmpty) {return getTicketId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getSubject() {return new FieldInfo(Ticket.class,"subject",String.class, null,p);}
	    public final String subject="subject";
	    public Specification<T> subject(Op op,Object value) {return getSubject().where(op,value);}
	    public Specification<T> subject(Op op,Object value,boolean chkEmpty) {return getSubject().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getBody() {return new FieldInfo(Ticket.class,"body",String.class, null,p);}
	    public final String body="body";
	    public Specification<T> body(Op op,Object value) {return getBody().where(op,value);}
	    public Specification<T> body(Op op,Object value,boolean chkEmpty) {return getBody().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCategory() {return new FieldInfo(Ticket.class,"category",TicketCategory.class, null,p);}
	    public final String category="category";
	    public Specification<T> category(Op op,Object value) {return getCategory().where(op,value);}
	    public Specification<T> category(Op op,Object value,boolean chkEmpty) {return getCategory().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPriority() {return new FieldInfo(Ticket.class,"priority",TicketPriority.class, null,p);}
	    public final String priority="priority";
	    public Specification<T> priority(Op op,Object value) {return getPriority().where(op,value);}
	    public Specification<T> priority(Op op,Object value,boolean chkEmpty) {return getPriority().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getStatus() {return new FieldInfo(Ticket.class,"status",TicketStatus.class, null,p);}
	    public final String status="status";
	    public Specification<T> status(Op op,Object value) {return getStatus().where(op,value);}
	    public Specification<T> status(Op op,Object value,boolean chkEmpty) {return getStatus().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getReply() {return new FieldInfo(Ticket.class,"reply",String.class, null,p);}
	    public final String reply="reply";
	    public Specification<T> reply(Op op,Object value) {return getReply().where(op,value);}
	    public Specification<T> reply(Op op,Object value,boolean chkEmpty) {return getReply().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getChannel() {return new FieldInfo(Ticket.class,"channel",String.class, null,p);}
	    public final String channel="channel";
	    public Specification<T> channel(Op op,Object value) {return getChannel().where(op,value);}
	    public Specification<T> channel(Op op,Object value,boolean chkEmpty) {return getChannel().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCustomer() {return new FieldInfo(Ticket.class,"customer",Member.class, null,p);}
	    public final String customer="customer";
	    public final $Member<T> customer() {return new $Member(getCustomer());}
	
	    public final FieldInfo<T> getOpenedAt() {return new FieldInfo(Ticket.class,"openedAt",java.time.LocalDateTime.class, null,p);}
	    public final String openedAt="openedAt";
	    public Specification<T> openedAt(Op op,Object value) {return getOpenedAt().where(op,value);}
	    public Specification<T> openedAt(Op op,Object value,boolean chkEmpty) {return getOpenedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getAnsweredAt() {return new FieldInfo(Ticket.class,"answeredAt",java.time.Instant.class, null,p);}
	    public final String answeredAt="answeredAt";
	    public Specification<T> answeredAt(Op op,Object value) {return getAnsweredAt().where(op,value);}
	    public Specification<T> answeredAt(Op op,Object value,boolean chkEmpty) {return getAnsweredAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getClosedAt() {return new FieldInfo(Ticket.class,"closedAt",java.time.Instant.class, null,p);}
	    public final String closedAt="closedAt";
	    public Specification<T> closedAt(Op op,Object value) {return getClosedAt().where(op,value);}
	    public Specification<T> closedAt(Op op,Object value,boolean chkEmpty) {return getClosedAt().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCustomerMemberId() {return new FieldInfo(Ticket.class,"customerMemberId",Long.class, null,p);}
	    public final String customerMemberId="customerMemberId";
	    public Specification<T> customerMemberId(Op op,Object value) {return getCustomerMemberId().where(op,value);}
	    public Specification<T> customerMemberId(Op op,Object value,boolean chkEmpty) {return getCustomerMemberId().where(op,value,chkEmpty);}
	}
	
	public static $KnowledgeArticle<KnowledgeArticle> KnowledgeArticle=new $KnowledgeArticle<>();
	public static class $KnowledgeArticle<T> {
	
	    private FieldInfo p=null;
	    public $KnowledgeArticle() {}
	    public $KnowledgeArticle(FieldInfo p) {this.p=p;}
	    public static JoinSpec<KnowledgeArticle> joinSpec() {
	    	return new JoinSpec<>(KnowledgeArticle.class);
	    }
	    public static UpdateSpec<KnowledgeArticle> updateSpec() {
	    	return new UpdateSpec<>(KnowledgeArticle.class);
	    }
	    public static DeleteSpec<KnowledgeArticle> deleteSpec() {
	    	return new DeleteSpec<>(KnowledgeArticle.class);
	    }
	
	    public final FieldInfo<T> getArticleId() {return new FieldInfo(KnowledgeArticle.class,"articleId",Long.class, null,p);}
	    public final String articleId="articleId";
	    public Specification<T> articleId(Op op,Object value) {return getArticleId().where(op,value);}
	    public Specification<T> articleId(Op op,Object value,boolean chkEmpty) {return getArticleId().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getTitle() {return new FieldInfo(KnowledgeArticle.class,"title",String.class, null,p);}
	    public final String title="title";
	    public Specification<T> title(Op op,Object value) {return getTitle().where(op,value);}
	    public Specification<T> title(Op op,Object value,boolean chkEmpty) {return getTitle().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getBody() {return new FieldInfo(KnowledgeArticle.class,"body",String.class, null,p);}
	    public final String body="body";
	    public Specification<T> body(Op op,Object value) {return getBody().where(op,value);}
	    public Specification<T> body(Op op,Object value,boolean chkEmpty) {return getBody().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getCategory() {return new FieldInfo(KnowledgeArticle.class,"category",TicketCategory.class, null,p);}
	    public final String category="category";
	    public Specification<T> category(Op op,Object value) {return getCategory().where(op,value);}
	    public Specification<T> category(Op op,Object value,boolean chkEmpty) {return getCategory().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getKeywords() {return new FieldInfo(KnowledgeArticle.class,"keywords",String.class, null,p);}
	    public final String keywords="keywords";
	    public Specification<T> keywords(Op op,Object value) {return getKeywords().where(op,value);}
	    public Specification<T> keywords(Op op,Object value,boolean chkEmpty) {return getKeywords().where(op,value,chkEmpty);}
	
	    public final FieldInfo<T> getPublished() {return new FieldInfo(KnowledgeArticle.class,"published",Boolean.class, null,p);}
	    public final String published="published";
	    public Specification<T> published(Op op,Object value) {return getPublished().where(op,value);}
	    public Specification<T> published(Op op,Object value,boolean chkEmpty) {return getPublished().where(op,value,chkEmpty);}
	}
	
}
