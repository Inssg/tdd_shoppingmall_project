package org.inssg.backend.cart;

import org.inssg.backend.item.Item;
import org.inssg.backend.item.ItemRepository;
import org.inssg.backend.member.Member;
import org.inssg.backend.member.MemberCreate;
import org.inssg.backend.member.MemberRepository;
import org.inssg.backend.security.userdetails.MemberDetails;
import org.inssg.backend.util.AcceptanceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.inssg.backend.util.ApiDocumentUtils.getRequestPreProcessor;
import static org.inssg.backend.util.ApiDocumentUtils.getResponsePreProcessor;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CartApiTest extends AcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;


    MemberDetails memberDetails;
    Cart cart;
    List<Item> items;

    @BeforeAll
    public void setup() {
        String email = "abc@gmail.com";
        String password = "1234";
        String username = "?????????";

        MemberCreate memberCreate = MemberCreate.builder().username(username).password(password).email(email).build();
        Member member = Member.create(memberCreate, passwordEncoder);
        memberRepository.save(member);
        memberDetails = MemberDetails.of(member);

        cart = Cart.builder().itemId(1L).memberId(1L).quantity(3).build();

        items = List.of(Item.create("????????????", "http://unplash.com", 4000),
                Item.create("?????????", "http://unpash2.com", 50000));



    }


    @Test
    @DisplayName("???????????? ?????? ??????")
    void ????????????_??????_??????() throws Exception {
        //given
        Long itemId = 1L;
        cartRepository.save(cart);
        given(cartService.addCartItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt()))
                .willReturn(cart);

        mockMvc.perform(post("/cart/items/{itemId}", itemId)
                        .param("quantity", "3")
                        .with(user(memberDetails))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.quantity").value(3))
                .andDo(document(
                        "add-cartItem",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("itemId").description("??????????????? ?????? ?????? ?????????")),
                        requestParameters(parameterWithName("quantity").description("??????????????? ?????? ????????????")),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("???????????? ?????????"),
                                        fieldWithPath("itemId").type(JsonFieldType.NUMBER).description("??????????????? ?????? ?????? ?????????"),
                                        fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("??????????????? ?????? ?????? ??????")
                                )
                        )
                ));

    }

    @Test
    @DisplayName("???????????? ??????")
    void ????????????_??????() throws Exception {
        //given
        itemRepository.saveAll(items);
        List<Cart> carts = List.of(Cart.builder().itemId(1L).memberId(1L).quantity(3).build(),
                                    Cart.builder().itemId(2L).memberId(1L).quantity(5).build());
        List<Cart> cartList = cartRepository.saveAll(carts);

        List<CartResponse> cartResponses = List.of(CartResponse.of(cartList.get(0), items.get(0)),
                CartResponse.of(cartList.get(1), items.get(1)));

        given(cartService.getCartItems(Mockito.anyLong()))
                .willReturn(cartResponses);

        mockMvc.perform(get("/cart/items")
                        .with(user(memberDetails))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].item.name").value("????????????"))
                .andExpect(jsonPath("$.[0].item.price").value(4000))
                .andExpect(jsonPath("$.[0].quantity").value(3))
                .andExpect(jsonPath("$.[1].item.name").value("?????????"))
                .andExpect(jsonPath("$.[1].item.price").value(50000))
                .andExpect(jsonPath("$.[1].quantity").value(5))
                .andDo(document(
                        "get-cartList",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        responseFields(
                                List.of(
                                    fieldWithPath("[].cartId").type(JsonFieldType.NUMBER).description("???????????? ?????????"),
                                    fieldWithPath("[].item").type(JsonFieldType.OBJECT).description("??????????????? ?????? ?????? ??????"),
                                    fieldWithPath("[].item.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                    fieldWithPath("[].item.name").type(JsonFieldType.STRING).description("?????? ??????"),
                                    fieldWithPath("[].item.imgPath").type(JsonFieldType.STRING).description("?????? ?????????"),
                                    fieldWithPath("[].item.price").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                    fieldWithPath("[].quantity").type(JsonFieldType.NUMBER).description("??????????????? ?????? ?????? ??????")
                                )
                        )
                ));
    }
    //Todo:Spring Rest docs requstHeader ????????????
    @Test
    @DisplayName("???????????? ??????")
    void ????????????_??????() throws Exception {
        //given
        Long itemId = 1L;
        willDoNothing().given(cartService).removeCartItem(Mockito.anyLong(),Mockito.anyLong());


        mockMvc.perform(delete("/cart/items/{itemId}", itemId)
                        .with(user(memberDetails)))
                .andExpect(status().isOk())
                .andDo(document(
                        "remove-cart",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("itemId").description("?????????????????? ????????? ?????? ?????????"))
                ));

       verify(cartService,atLeastOnce()).removeCartItem(Mockito.anyLong(),Mockito.anyLong());
    }


}
