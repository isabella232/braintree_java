package com.braintreegateway.integrationtest;

import com.braintreegateway.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class MerchantAccountIT {

    private BraintreeGateway gateway;

    @Before
    public void createGateway() {
        this.gateway = new BraintreeGateway(Environment.DEVELOPMENT, "integration_merchant_id", "integration_public_key",
                "integration_private_key");
    }

    @Test
    public void deprecatedCreateSucceeds() {
        Result<MerchantAccount> result = gateway.merchantAccount().create(deprecatedCreationRequest());

        assertTrue("merchant account creation should succeed", result.isSuccess());

        MerchantAccount ma = result.getTarget();
        assertEquals("account status should be pending", MerchantAccount.Status.PENDING, ma.getStatus());
        assertEquals("sandbox_master_merchant_account", ma.getMasterMerchantAccount().getId());
        assertTrue(ma.isSubMerchant());
        assertFalse(ma.getMasterMerchantAccount().isSubMerchant());
    }

    @Test
    public void createRequiresNoId() {
        Result<MerchantAccount> result = gateway.merchantAccount().create(creationRequest());

        assertTrue("merchant account creation should succeed", result.isSuccess());

        MerchantAccount ma = result.getTarget();
        assertEquals("account status should be pending", MerchantAccount.Status.PENDING, ma.getStatus());
        assertEquals("sandbox_master_merchant_account", ma.getMasterMerchantAccount().getId());
        assertTrue(ma.isSubMerchant());
        assertFalse(ma.getMasterMerchantAccount().isSubMerchant());
    }

    @Test
    public void createWillUseIdIfPassed() {
        int randomNumber = new Random().nextInt() % 10000;
        String subMerchantAccountId = String.format("sub_merchant_account_id_%d", randomNumber);
        MerchantAccountRequest request = creationRequest().id(subMerchantAccountId);
        Result<MerchantAccount> result = gateway.merchantAccount().create(request);

        assertTrue("merchant account creation should succeed", result.isSuccess());
        MerchantAccount ma = result.getTarget();
        assertEquals("account status should be pending", MerchantAccount.Status.PENDING, ma.getStatus());
        assertEquals("submerchant id should be assigned", subMerchantAccountId, ma.getId());
        assertEquals("sandbox_master_merchant_account", ma.getMasterMerchantAccount().getId());
        assertTrue(ma.isSubMerchant());
        assertFalse(ma.getMasterMerchantAccount().isSubMerchant());
    }

    @Test
    public void handlesUnsuccessfulResults() {
        Result<MerchantAccount> result = gateway.merchantAccount().create(new MerchantAccountRequest());
        List<ValidationError> errors = result.getErrors().forObject("merchant-account").onField("master_merchant_account_id");
        assertEquals(1, errors.size());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_MASTER_MERCHANT_ACCOUNT_ID_IS_REQUIRED, errors.get(0).getCode());
    }

    @Test
    public void createAcceptsBankFundingDestination() {
        MerchantAccountRequest request = creationRequest().
            funding().
                destination(MerchantAccount.FundingDestination.BANK).
                routingNumber("122100024").
                accountNumber("98479798798").
                done();
        Result<MerchantAccount> result = gateway.merchantAccount().create(request);

        assertTrue("merchant account creation should succeed", result.isSuccess());
    }

    @Test
    public void createAcceptsEmailFundingDestination() {
        MerchantAccountRequest request = creationRequest().
            funding().
                destination(MerchantAccount.FundingDestination.EMAIL).
                email("joe@bloggs.com").
                done();
        Result<MerchantAccount> result = gateway.merchantAccount().create(request);

        assertTrue("merchant account creation should succeed", result.isSuccess());
    }

    @Test
    public void createAcceptsMobilePhoneFundingDestination() {
        MerchantAccountRequest request = creationRequest().
            funding().
                destination(MerchantAccount.FundingDestination.MOBILE_PHONE).
                mobilePhone("3125551212").
                done();
        Result<MerchantAccount> result = gateway.merchantAccount().create(request);

        assertTrue("merchant account creation should succeed", result.isSuccess());
    }

    @Test
    public void updateUpdatesAllFields() {
        Result<MerchantAccount> result = gateway.merchantAccount().create(deprecatedCreationRequest());
        assertTrue("merchant account creation should succeed", result.isSuccess());
        MerchantAccountRequest request = creationRequest().
            masterMerchantAccountId(null);
        Result<MerchantAccount> update_result = gateway.merchantAccount().update(result.getTarget().getId(), request);
        assertTrue("merchant account update should succeed", update_result.isSuccess());
        MerchantAccount merchant_account = update_result.getTarget();
        assertEquals("Job", merchant_account.getIndividualDetails().getFirstName());
        assertEquals("Leoggs", merchant_account.getIndividualDetails().getLastName());
        assertEquals("job@leoggs.com", merchant_account.getIndividualDetails().getEmail());
        assertEquals("5555551212", merchant_account.getIndividualDetails().getPhone());
        assertEquals("193 Credibility St.", merchant_account.getIndividualDetails().getAddress().getStreetAddress());
        assertEquals("60611", merchant_account.getIndividualDetails().getAddress().getPostalCode());
        assertEquals("Avondale", merchant_account.getIndividualDetails().getAddress().getLocality());
        assertEquals("IN", merchant_account.getIndividualDetails().getAddress().getRegion());
        assertEquals("1985-09-10", merchant_account.getIndividualDetails().getDateOfBirth());
        assertEquals("Calculon", merchant_account.getBusinessDetails().getLegalName());
        assertEquals("Calculon", merchant_account.getBusinessDetails().getDbaName());
        assertEquals("123456780", merchant_account.getBusinessDetails().getTaxId());
        assertEquals("135 Credibility St.", merchant_account.getBusinessDetails().getAddress().getStreetAddress());
        assertEquals("60602", merchant_account.getBusinessDetails().getAddress().getPostalCode());
        assertEquals("Gary", merchant_account.getBusinessDetails().getAddress().getLocality());
        assertEquals("OH", merchant_account.getBusinessDetails().getAddress().getRegion());
        assertEquals(MerchantAccount.FundingDestination.EMAIL, merchant_account.getFundingDetails().getDestination());
        assertEquals("joe+funding@bloggs.com", merchant_account.getFundingDetails().getEmail());
        assertEquals("3125551212", merchant_account.getFundingDetails().getMobilePhone());
        assertEquals("122100024", merchant_account.getFundingDetails().getRoutingNumber());
        assertEquals("8799", merchant_account.getFundingDetails().getAccountNumberLast4());
    }

    @Test
    public void createHandlesRequiredValidationErrors() {
        MerchantAccountRequest request = new MerchantAccountRequest().
            tosAccepted(true).
            masterMerchantAccountId("sandbox_master_merchant_account");
        Result<MerchantAccount> result = gateway.merchantAccount().create(request);
        assertFalse(result.isSuccess());
        ValidationErrors errors = result.getErrors().forObject("merchant-account");
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_FIRST_NAME_IS_REQUIRED,
            errors.forObject("individual").onField("first-name").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_LAST_NAME_IS_REQUIRED,
            errors.forObject("individual").onField("last-name").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_DATE_OF_BIRTH_IS_REQUIRED,
            errors.forObject("individual").onField("date-of-birth").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_EMAIL_IS_REQUIRED,
            errors.forObject("individual").onField("email").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_ADDRESS_STREET_ADDRESS_IS_REQUIRED,
            errors.forObject("individual").forObject("address").onField("street-address").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_ADDRESS_LOCALITY_IS_REQUIRED,
            errors.forObject("individual").forObject("address").onField("locality").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_ADDRESS_POSTAL_CODE_IS_REQUIRED,
            errors.forObject("individual").forObject("address").onField("postal-code").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_ADDRESS_REGION_IS_REQUIRED,
            errors.forObject("individual").forObject("address").onField("region").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_FUNDING_DESTINATION_IS_REQUIRED,
            errors.forObject("funding").onField("destination").get(0).getCode());
    }

    @Test
    public void createHandlesInvalidValidationErrors() {
        MerchantAccountRequest request = new MerchantAccountRequest().
            individual().
                firstName("<>").
                lastName("<>").
                email("bad").
                phone("999").
                address().
                    streetAddress("nope").
                    postalCode("1").
                    region("QQ").
                    done().
                dateOfBirth("hah").
                ssn("12345").
                done().
            business().
                taxId("bad").
                dbaName("{}``").
                legalName("``{}").
                address().
                    streetAddress("nope").
                    postalCode("1").
                    region("QQ").
                    done().
                done().
            funding().
                destination(MerchantAccount.FundingDestination.UNRECOGNIZED).
                routingNumber("LEATHER").
                accountNumber("BACK POCKET").
                email("BILLFOLD").
                mobilePhone("TRIFOLD").
                done().
            tosAccepted(true).
            masterMerchantAccountId("sandbox_master_merchant_account");
        Result<MerchantAccount> result = gateway.merchantAccount().create(request);
        assertFalse(result.isSuccess());
        ValidationErrors errors = result.getErrors().forObject("merchant-account");
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_FIRST_NAME_IS_INVALID,
            errors.forObject("individual").onField("first-name").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_LAST_NAME_IS_INVALID,
            errors.forObject("individual").onField("last-name").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_DATE_OF_BIRTH_IS_INVALID,
            errors.forObject("individual").onField("date-of-birth").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_PHONE_IS_INVALID,
            errors.forObject("individual").onField("phone").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_SSN_IS_INVALID,
            errors.forObject("individual").onField("ssn").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_EMAIL_IS_INVALID,
            errors.forObject("individual").onField("email").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_ADDRESS_STREET_ADDRESS_IS_INVALID,
            errors.forObject("individual").forObject("address").onField("street-address").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_ADDRESS_POSTAL_CODE_IS_INVALID,
            errors.forObject("individual").forObject("address").onField("postal-code").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_INDIVIDUAL_ADDRESS_REGION_IS_INVALID,
            errors.forObject("individual").forObject("address").onField("region").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_BUSINESS_DBA_NAME_IS_INVALID,
            errors.forObject("business").onField("dba-name").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_BUSINESS_LEGAL_NAME_IS_INVALID,
            errors.forObject("business").onField("legal-name").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_BUSINESS_TAX_ID_IS_INVALID,
            errors.forObject("business").onField("tax-id").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_BUSINESS_ADDRESS_STREET_ADDRESS_IS_INVALID,
            errors.forObject("business").forObject("address").onField("street-address").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_BUSINESS_ADDRESS_POSTAL_CODE_IS_INVALID,
            errors.forObject("business").forObject("address").onField("postal-code").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_BUSINESS_ADDRESS_REGION_IS_INVALID,
            errors.forObject("business").forObject("address").onField("region").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_FUNDING_DESTINATION_IS_INVALID,
            errors.forObject("funding").onField("destination").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_FUNDING_ACCOUNT_NUMBER_IS_INVALID,
            errors.forObject("funding").onField("account-number").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_FUNDING_ROUTING_NUMBER_IS_INVALID,
            errors.forObject("funding").onField("routing-number").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_FUNDING_EMAIL_IS_INVALID,
            errors.forObject("funding").onField("email").get(0).getCode());
        assertEquals(ValidationErrorCode.MERCHANT_ACCOUNT_FUNDING_MOBILE_PHONE_IS_INVALID,
            errors.forObject("funding").onField("mobile-phone").get(0).getCode());
    }

    private MerchantAccountRequest deprecatedCreationRequest() {
        return new MerchantAccountRequest().
            applicantDetails().
                firstName("Joe").
                lastName("Bloggs").
                email("joe@bloggs.com").
                phone("555-555-5555").
                address().
                    streetAddress("123 Credibility St.").
                    postalCode("60606").
                    locality("Chicago").
                    region("IL").
                    done().
                dateOfBirth("10/9/1980").
                ssn("123-45-7890").
                routingNumber("122100024").
                accountNumber("98479798798").
                taxId("123456789").
                companyName("Calculon's Drama School").
                done().
            tosAccepted(true).
            masterMerchantAccountId("sandbox_master_merchant_account");
    }

    private MerchantAccountRequest creationRequest() {
        return new MerchantAccountRequest().
            individual().
                firstName("Job").
                lastName("Leoggs").
                email("job@leoggs.com").
                phone("555-555-1212").
                address().
                    streetAddress("193 Credibility St.").
                    postalCode("60611").
                    locality("Avondale").
                    region("IN").
                    done().
                dateOfBirth("10/9/1985").
                ssn("123-45-1235").
                done().
            business().
                taxId("123456780").
                dbaName("Calculon").
                legalName("Calculon").
                address().
                    streetAddress("135 Credibility St.").
                    postalCode("60602").
                    locality("Gary").
                    region("OH").
                    done().
                done().
            funding().
                destination(MerchantAccount.FundingDestination.EMAIL).
                routingNumber("122100024").
                accountNumber("98479798799").
                email("joe+funding@bloggs.com").
                mobilePhone("3125551212").
                done().
            tosAccepted(true).
            masterMerchantAccountId("sandbox_master_merchant_account");
    }
}

