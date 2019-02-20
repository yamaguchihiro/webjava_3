package jp.co.systena.tigerscave.ShoppingSite.application.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jp.co.systena.tigerscave.ShoppingSite.application.model.Cart;
import jp.co.systena.tigerscave.ShoppingSite.application.model.Item;
import jp.co.systena.tigerscave.ShoppingSite.application.model.ListForm;
import jp.co.systena.tigerscave.ShoppingSite.application.model.Order;
import jp.co.systena.tigerscave.ShoppingSite.application.service.ListService;

@Controller // Viewあり。Viewを返却するアノテーション

public class ListController {
  @Autowired
  HttpSession session;

  private final JdbcTemplate jdbcTemplate;
  @Autowired
  public ListController(JdbcTemplate jdbcTemplate) {
      this.jdbcTemplate = jdbcTemplate;
  }


  /**
   * ユーザの追加（GET、フォームを表示）
   */
  @GetMapping("/admin/add_user")
  public String adminAddUserGet() {
      return "/add_user";
  }

  /**
   * ユーザの追加（POST、データベースを操作）
   */
  @PostMapping("/admin/add_user")
  public String adminAddUserPost(@RequestParam("username") String username,
                                 @RequestParam("password") String password,
                                 @RequestParam("role") String role) {
      PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      jdbcTemplate.update("INSERT INTO users ( username, password , enabled ) VALUES (?, ?, ?)", username, passwordEncoder.encode(password), true);
      jdbcTemplate.update("INSERT INTO authorities ( username, authority) VALUES (?, ?)", username, role);
      return "redirect:/admin/add_user";
  }


  /**
   * ログインフォームを表示する
   */
  @GetMapping({"/login","/login?{param}"})
  public ModelAndView login(ModelAndView mav,@RequestParam(name = "error", required = false) String error) {

	  String message;

	  if(error  == null) {
		  message = "";
	  }else {
		  System.out.println("suc");
		  message = "ログインに失敗しました。";
	  }

	  mav.addObject("message", message);

	  mav.setViewName("Login");
	    return mav;
  }


  @RequestMapping(value = {"/ShoppingSite/ListView"}, method = {RequestMethod.GET}) // URLとのマッピング

  public ModelAndView show(ModelAndView mav) {
    // Viewに渡すデータを設定
    // セッション情報から保存したデータを取得してメッセージを生成
    ListForm userForm = (ListForm) session.getAttribute("form");
    session.removeAttribute("form");
    if (userForm != null) {
      mav.addObject("message", userForm.getItem().getName()+"を購入しました。単価は"+userForm.getItem().getPrice()+"円です。購入個数は"+userForm.getNum()+"個です。");
    }

    List<Item> itemlist = ListService.getItemList();

    Cart cart = (Cart) session.getAttribute("CurrentCart");

    if( cart == null) {
      cart = new Cart();
      session.setAttribute("CurrentCart", cart);
    }

    mav.addObject(cart);
    mav.addObject("TotalPrice",cart.calOrderTotalPrice());
    mav.addObject("ListForm", new ListForm());  // 新規クラスを設定
    mav.addObject("itemlist", itemlist);

    BindingResult bindingResult = (BindingResult) session.getAttribute("result");
    if (bindingResult != null) {
      mav.addObject("bindingResult", bindingResult);
    }
    // Viewのテンプレート名を設定
    mav.setViewName("shoppingsite/ListView");
    return mav;
  }

  @RequestMapping(value="/ShoppingSite/ListView", method = RequestMethod.POST)  // URLとのマッピング
  private ModelAndView order(ModelAndView mav, @Valid ListForm ListForm, BindingResult bindingResult, HttpServletRequest request) {

    Cart cart = (Cart) session.getAttribute("CurrentCart");

    if( cart == null) {
      cart = new Cart();
        session.setAttribute("CurrentCart", cart);
    }

    if (bindingResult.getAllErrors().size() > 0) {

      System.out.println(bindingResult.getAllErrors().toString());

      // エラーがある場合はそのまま戻す
      mav.addObject("ListForm", new ListForm());  // 新規クラスを設定
      // Viewのテンプレート名を設定
      return new ModelAndView("redirect:/ShoppingSite/ListView");
    }

    Order order = new Order();
    order.setItem(ListForm.getItem());
    order.setNum(ListForm.getNum());
    cart.addOrderlist(order);
    // データをセッションへ保存
    session.setAttribute("form", ListForm);
    session.setAttribute("CurrentCart", cart);
    return new ModelAndView("redirect:/ShoppingSite/ListView");        // リダイレクト
  }
}
