package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.model.events.ValidateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum>
{
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute( final StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context )
    {
        String beerOrderId = context.getMessage().getHeaders().get( BeerOrderManagerImpl.BEER_ORDER_ID_HEADER ).toString();
        BeerOrder beerOrder = Optional.ofNullable( beerOrderRepository.findById( UUID.fromString( beerOrderId ) ))
                                      .map( optionalBeerOrder -> optionalBeerOrder.get() )
                                      .orElse( null );

        jmsTemplate.convertAndSend( JmsConfig.VALIDATE_ORDER_QUEUE, ValidateBeerOrderRequest.builder()
                                                                                            .beerOrderDto( beerOrderMapper.beerOrderToDto( beerOrder ) )
                                                                                            .build() );
        log.debug("Sent validation request to queue for id '{}", beerOrderId);

    }
}

