package jpabook.jpashop.service;

import jpabook.jpashop.domain.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    /**
     * 상품 등록
     * @param item
     * @return Long
     */
    @Transactional
    public Long saveItem(Item item){
        itemRepository.save(item);
        return item.getId();
    }

    /**
     * 상품 단건 조회
     * @param id
     * @return Item
     */
    public Item findOne(Long id){
        return itemRepository.findOne(id);
    }

    /**
     * 상품 전체 조회
     * @return List<Item>
     */
    public List<Item> findItems(){
        return itemRepository.findAll();
    }
}
