package ru.playa.keycloak.modules;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

import java.util.UUID;

public class PasswordUtils {

    public static String get() {
//        PasswordGenerator gen = new PasswordGenerator();
//
//        CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase, 5);
//        CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase, 5);
//        CharacterRule digitRule = new CharacterRule(EnglishCharacterData.Digit, 5);

//        return gen.generatePassword(50, lowerCaseRule, upperCaseRule, digitRule);

        return UUID.randomUUID().toString() + UUID.randomUUID();
    }


}
