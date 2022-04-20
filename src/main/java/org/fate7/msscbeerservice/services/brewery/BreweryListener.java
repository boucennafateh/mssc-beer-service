package org.fate7.msscbeerservice.services.brewery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fate7.msscbeerservice.Domain.Beer;
import org.fate7.msscbeerservice.config.JmsConfig;
import org.fate7.msscbeerservice.events.BrewBeerEvent;
import org.fate7.msscbeerservice.events.NewInventoryEvent;
import org.fate7.msscbeerservice.repositories.BeerRepository;
import org.fate7.msscbeerservice.web.model.BeerDto;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BreweryListener {

    private final BeerRepository beerRepository;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.BREW_REQUEST_QUEUE)
    public void listener(BrewBeerEvent brewBeerEvent){
        BeerDto beerDto = brewBeerEvent.getBeerDto();
        Beer beer = beerRepository.findById(beerDto.getId())
                .orElseThrow(() -> new RuntimeException("beer id not found"));
        beerDto.setQuantityOnHand(beer.getQuantityToBrew());
        NewInventoryEvent inventoryEvent = new NewInventoryEvent(beerDto);
        log.debug("sending beerDto to " + JmsConfig.NEW_INVENTORY_QUEUE + " queue");
        jmsTemplate.convertAndSend(JmsConfig.NEW_INVENTORY_QUEUE, inventoryEvent);

    }
}