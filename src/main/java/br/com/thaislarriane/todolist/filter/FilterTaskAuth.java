package br.com.thaislarriane.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.thaislarriane.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

        //validar rota
        var serverletPath = request.getServletPath();

        if(!serverletPath.equals("/tasks/")){
            filterChain.doFilter(request, response);
            return;
        }   

        //Pegar a autenticação: usuário e senha
        var authorization = request.getHeader("Authorization");

        var authEncoded = authorization.substring("Basic".length()).trim();

        byte[] authDecode = Base64.getDecoder().decode(authEncoded);

        var authString = new String(authDecode);

        String[] credentials = authString.split(":");
        String username = credentials[0];
        String password = credentials[1];

        //validar usuário
        var user = this.userRepository.findByUsername(username);

        if(user == null){
            response.sendError(401);
            return;
        } 
        
        //validar senha
        var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        request.setAttribute("idUser", user.getId());

        if(!passwordVerify.verified){
            response.sendError(401);
            return;
        }  
        filterChain.doFilter(request, response);        
        
    }
 
}