package com.microservices.user.service;

import com.microservices.user.VO.Department;
import com.microservices.user.VO.ResponseTemplateVO;
import com.microservices.user.entity.User;
import com.microservices.user.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserService {

    private static final String USER_SERVICE = "userService";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public User saveUser(User user) {
        log.info("Inside saveUser of UserService");
        return userRepository.save(user);
    }

    @CircuitBreaker(name=USER_SERVICE,fallbackMethod = "userServiceFallBackMethod" )
    public ResponseTemplateVO getUserWithDepartment(Long userId) {
        log.info("Inside getUserWithDepartment of UserService");
        ResponseTemplateVO vo = new ResponseTemplateVO();
        User user = userRepository.findByUserId(userId);

        Department department =
                restTemplate.getForObject("http://DEPARTMENT-SERVICE/departments/" + user.getDepartmentId()
                        ,Department.class);

        vo.setUser(user);
        vo.setDepartment(department);

        return  vo;
    }
    public ResponseTemplateVO userServiceFallBackMethod(Exception e)
    {
        ResponseTemplateVO vo = new ResponseTemplateVO();
        log.info("Inside Exception of UserService");
        User user = new User();
        user.setUserId(0L);
        user.setEmail("Internal Error Please try later");
        user.setFirstName("Internal Error Please try later");
        user.setLastName("Internal Error Please try later");

        Department department =new Department();
        department.setDepartmentId(0L);
        department.setDepartmentAddress("Internal Error Please try later");
        department.setDepartmentName("Internal Error Please try later");
        department.setDepartmentCode("Internal Error Please try later");

        vo.setUser(user);
        vo.setDepartment(department);

        return vo ;
    }
}