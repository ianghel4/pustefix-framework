/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.example.bank.ihandler;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.example.bank.AuthTokenManager;
import de.schlund.pfixcore.example.bank.BankApplication;
import de.schlund.pfixcore.example.bank.StatusCodeLib;
import de.schlund.pfixcore.example.bank.context.ContextAccount;
import de.schlund.pfixcore.example.bank.context.ContextCustomer;
import de.schlund.pfixcore.example.bank.iwrapper.Login;
import de.schlund.pfixcore.example.bank.model.Account;
import de.schlund.pfixcore.example.bank.model.BankDAO;
import de.schlund.pfixcore.example.bank.model.Customer;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;


public class LoginHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Login login = (Login) wrapper;
        BankDAO bankDAO = BankApplication.getInstance().getBankDAO();
        if (login.getCustomerID() != null) {
            Customer customer = null;
            try {
                long customerId = Long.parseLong(login.getCustomerID());
                customer = bankDAO.getCustomerById(customerId);
                String password = login.getPassword();
                if (password == null || !password.equals(customer.getPassword())) customer = null;
            } catch (NumberFormatException x) {}
            if (customer == null) {
                login.addSCodeCustomerID(StatusCodeLib.ILLEGAL_LOGIN);
                login.addSCodePassword(StatusCodeLib.ILLEGAL_LOGIN);
            } else {
                ContextCustomer contextCustomer = context.getContextResourceManager().getResource(ContextCustomer.class);
                contextCustomer.setCustomer(customer);
                Authentication auth = context.getAuthentication();
                auth.addRole("UNRESTRICTED");
            }
        } else if (login.getAuthToken() != null) {
            String[] tokens = AuthTokenManager.decodeAuthToken(login.getAuthToken());
            if (tokens.length == 2) {
                try {
                    Long cid = Long.parseLong(tokens[0]);
                    Customer customer = bankDAO.getCustomerById(cid);
                    if (customer != null) {
                        Long aid = Long.parseLong(tokens[1]);
                        Account account = customer.getAccountByNo(aid);
                        if (account != null) {
                            ContextCustomer contextCustomer = context.getContextResourceManager().getResource(ContextCustomer.class);
                            contextCustomer.setCustomer(customer);
                            Authentication auth = context.getAuthentication();
                            auth.addRole("ACCOUNT");
                            ContextAccount contextAccount = context.getContextResourceManager().getResource(ContextAccount.class);
                            contextAccount.setAccount(account);
                            return;
                        }
                    }
                } catch (NumberFormatException x) {}
            }
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException x) {}
            throw new IllegalArgumentException("Illegal auth token.");
        }
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return false;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {

    }

}
