package se.sensera.banking.impl;


import se.sensera.banking.User;
import se.sensera.banking.exceptions.Activity;
import se.sensera.banking.exceptions.UseException;
import se.sensera.banking.exceptions.UseExceptionType;

public class UserExceptionHandlingFacade {

    public User handleCreateUser(User user, boolean notUnique, int route, String personalIdentificationNumber) throws UseException {
        if (notUnique && route == 1) {
            throw new UseException(Activity.CREATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        } else if (notUnique && route == 2) {
            throw new UseException(Activity.UPDATE_USER, UseExceptionType.USER_PERSONAL_ID_NOT_UNIQUE);
        } else {
            user.setPersonalIdentificationNumber(personalIdentificationNumber);
            return user;
        }
/*    public user handleInactiveUser(String userId){
            if(){
//
            }else{
                //.orElseThrow(() -> new UseException(Activity.UPDATE_USER, UseExceptionType.NOT_FOUND));
            }
            return User;

        }*/
    }
}
