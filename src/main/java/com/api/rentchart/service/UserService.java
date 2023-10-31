package com.api.rentchart.service;

import com.api.rentchart.entities.User;
import com.api.rentchart.exceptions.EtAuthException;
import com.api.rentchart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User validateUser(String email, String username, String password) throws EtAuthException{
        if(email != null) email = email.toLowerCase();
        return userRepository.findByEmailAndPassword(email, username, password);
    }

    public User registerUser(String email, String username, String password) throws EtAuthException {

        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        if(email != null) email = email.toLowerCase();
        if(!pattern.matcher(email).matches())
            throw new EtAuthException("Formato de email invalido");
        Integer countEmail = userRepository.getCountByEmail(email);
        if(countEmail > 0)
            throw new EtAuthException("Email ya existente");
        Integer countUsername = userRepository.getCountByUsername(username);
        if(countUsername > 0)
            throw new EtAuthException("Username ya existente");
        UUID id = userRepository.create(email, username, password);
        return userRepository.findById(id);
    }

}
