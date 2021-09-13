package com.gmoon.springframework.inject;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class MyRepository implements PooRepository {
}
