package ru.finkontrol.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
  private final int BG=0xff0a1012, PANEL=0xff182326, PANEL2=0xff213034, WHITE=0xfff2f7f4;
  private final int MINT=0xffb8efcf, GREEN=0xff75cba1, MUTED=0xff95a9a9, BLUE=0xff88a9df, GOLD=0xffdfbf87, RED=0xffffaaa0;
  private SharedPreferences prefs;
  private LinearLayout page, nav, root;
  private int screen=0;
  private JSONArray expenses, goals;
  private double income, loans, savings;
  private final NumberFormat nf=NumberFormat.getNumberInstance(new Locale("ru","RU"));
  private int dp(float f){return (int)(f*getResources().getDisplayMetrics().density+0.5f);}
  private String money(double n){return nf.format(Math.round(n))+" ₽";}
  private double expenseTotal(){
    double x=0; for(int i=0;i<expenses.length();i++)x+=expenses.optJSONObject(i).optDouble("amount",0);return x;
  }
  private double free(){return income-expenseTotal()-loans;}
  private void load(){
    income=Double.longBitsToDouble(prefs.getLong("income",Double.doubleToLongBits(100000)));
    loans=Double.longBitsToDouble(prefs.getLong("loans",Double.doubleToLongBits(20000)));
    savings=Double.longBitsToDouble(prefs.getLong("savings",Double.doubleToLongBits(30000)));
    try {
      String d=prefs.getString("expenses",null);
      if(d==null){expenses=new JSONArray();
        addJson(expenses,"Продукты",16000);addJson(expenses,"Коммунальные услуги",9000);
        addJson(expenses,"Транспорт",7000);addJson(expenses,"Разное",8000);
      }else expenses=new JSONArray(d);
      String g=prefs.getString("goals",null);
      if(g==null){goals=new JSONArray();JSONObject o=new JSONObject();o.put("name","Отпуск");o.put("target",120000);o.put("saved",35000);goals.put(o);}
      else goals=new JSONArray(g);
    }catch(Exception e){expenses=new JSONArray();goals=new JSONArray();}
  }
  private void addJson(JSONArray a,String name,double amt)throws Exception{
    JSONObject o=new JSONObject();o.put("name",name);o.put("amount",amt);a.put(o);
  }
  private void save(){
    prefs.edit().putLong("income",Double.doubleToLongBits(income))
      .putLong("loans",Double.doubleToLongBits(loans))
      .putLong("savings",Double.doubleToLongBits(savings))
      .putString("expenses",expenses.toString()).putString("goals",goals.toString()).apply();
  }
  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
    prefs=getSharedPreferences("fincontrol_private",MODE_PRIVATE);load();render();
  }
  private TextView text(String s,int size,int color,boolean bold){
    TextView t=new TextView(this); t.setText(s);t.setTextColor(color);t.setTextSize(size);
    t.setGravity(Gravity.CENTER_VERTICAL);
    if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
    return t;
  }
  private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
  private void pad(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
  private android.graphics.drawable.GradientDrawable bg(int color,int radius){
    android.graphics.drawable.GradientDrawable b=new android.graphics.drawable.GradientDrawable();
    b.setColor(color);b.setCornerRadius(dp(radius));return b;
  }
  private LinearLayout box(int color){LinearLayout p=vertical();p.setBackground(bg(color,22));pad(p,19,19);return p;}
  private void marginBottom(LinearLayout parent, View child,int bottom){
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(bottom);parent.addView(child,lp);
  }
  private void line(LinearLayout p,int color){
    View v=new View(this);v.setBackgroundColor(color);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.topMargin=dp(13);lp.bottomMargin=dp(13);p.addView(v,lp);
  }
  private Button button(String name, final Runnable r, boolean primary){
    Button b=new Button(this);b.setText(name);b.setTextAllCaps(false);b.setTextSize(13);
    b.setTextColor(primary?0xff122b22:WHITE);b.setBackground(bg(primary?MINT:PANEL2,13));
    b.setOnClickListener(v->r.run());return b;
  }
  private void head(String caption,String title,String subtitle){
    TextView label=text(caption.toUpperCase(new Locale("ru")),11,GREEN,true);
    label.setLetterSpacing(.14f);marginBottom(page,label,10);
    marginBottom(page,text(title,32,WHITE,true),4);
    marginBottom(page,text(subtitle,13,MUTED,false),21);
  }
  private LinearLayout section(String title){
    LinearLayout c=box(PANEL);marginBottom(c,text(title,18,WHITE,true),17);marginBottom(page,c,14);return c;
  }
  private void rowInfo(LinearLayout p,String label,String value,int color){
    LinearLayout r=horizontal();
    TextView l=text(label,13,MUTED,false);
    TextView a=text(value,14,color,true);a.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
    r.addView(l,new LinearLayout.LayoutParams(0,dp(32),1));
    r.addView(a,new LinearLayout.LayoutParams(-2,dp(32)));
    p.addView(r);
  }
  private void render(){
    root=vertical();root.setBackgroundColor(BG);setContentView(root);
    LinearLayout bar=horizontal();pad(bar,20,13);
    TextView brand=text("↗  ФинКонтроль",19,WHITE,true);bar.addView(brand,new LinearLayout.LayoutParams(0,dp(40),1));
    Button edit=button("⚙",()->settings(),false);bar.addView(edit,new LinearLayout.LayoutParams(dp(54),dp(42)));
    root.addView(bar);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
    page=vertical();page.setPadding(dp(17),dp(18),dp(17),dp(18));scroll.addView(page);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    if(screen==0)overview();if(screen==1)expenseScreen();if(screen==2)goalScreen();if(screen==3)adviceScreen();
    nav=horizontal();nav.setBackgroundColor(PANEL);pad(nav,7,9);
    String[] titles={"⌂\nОбзор","▤\nРасходы","◎\nЦели","✦\nСоветы"};
    for(int i=0;i<titles.length;i++){
      final int idx=i;
      TextView t=text(titles[i],12,i==screen?MINT:MUTED,i==screen);
      t.setGravity(Gravity.CENTER);t.setOnClickListener(v->{screen=idx;render();});
      nav.addView(t,new LinearLayout.LayoutParams(0,dp(54),1));
    }
    root.addView(nav);
  }
  private void overview(){
    head("Личные финансы","Мой бюджет","Всё важное — на одном экране.");
    LinearLayout hero=box(0xff204739);marginBottom(hero,text("Свободно в этом месяце",13,0xffd3e5da,false),14);
    marginBottom(hero,text(money(free()),39,free()<0?RED:WHITE,true),13);
    marginBottom(hero,text(free()<0?"●  Расходы выше доходов":free()<income*.15?"●  Есть риск перегрузки":"✓  Финансовый баланс в норме",12,free()<0?RED:MINT,true),12);
    line(hero,0xff406854);
    marginBottom(hero,button("Изменить бюджет →",()->settings(),true),2);
    marginBottom(page,hero,14);
    LinearLayout stats=horizontal();
    String[] name={"Доход","Расходы","Кредиты"};
    double[] vals={income,expenseTotal(),loans};
    for(int i=0;i<3;i++){
      LinearLayout cell=vertical();cell.setBackground(bg(PANEL,15));pad(cell,10,12);
      marginBottom(cell,text(name[i],11,MUTED,false),7);
      cell.addView(text(money(vals[i]),15,i==0?MINT:WHITE,true));
      LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(91),1);lp.leftMargin=dp(i==0?0:6);
      stats.addView(cell,lp);
    }marginBottom(page,stats,14);
    LinearLayout split=section("Структура бюджета");
    split.addView(new DonutView(this,expenseTotal(),loans,Math.max(0,free())),new LinearLayout.LayoutParams(-1,dp(190)));
    rowInfo(split,"Повседневные расходы",money(expenseTotal()),GREEN);
    rowInfo(split,"Платежи по кредитам",money(loans),BLUE);
    rowInfo(split,"Свободные средства",money(free()),free()<0?RED:MINT);
    LinearLayout forecast=section("Прогноз на 6 месяцев");
    forecast.addView(new ForecastView(this,Math.max(0,savings),free()),new LinearLayout.LayoutParams(-1,dp(175)));
    marginBottom(forecast,text("Условный прогноз при сохранении текущих сумм.",11,MUTED,false),2);
    LinearLayout tips=section("Рекомендация");
    marginBottom(tips,text(adviceText(),13,WHITE,false),11);
    tips.addView(button("Все рекомендации →",()->{screen=3;render();},false));
  }
  private void expenseScreen(){
    head("Контроль расходов","Мои траты","Добавляйте категории — итоги обновятся.");
    LinearLayout sum=box(0xff204739);
    marginBottom(sum,text("Расходы в месяц",12,MUTED,false),8);
    marginBottom(sum,text(money(expenseTotal()),31,WHITE,true),12);
    sum.addView(button("+ Добавить расход",()->addExpense(),true));marginBottom(page,sum,16);
    LinearLayout list=section("Статьи расходов");
    if(expenses.length()==0)marginBottom(list,text("Расходов пока нет.",13,MUTED,false),8);
    for(int i=0;i<expenses.length();i++){
      JSONObject o=expenses.optJSONObject(i);if(o==null)continue;
      final int idx=i;
      LinearLayout r=horizontal();
      TextView n=text(o.optString("name","Расход"),13,WHITE,true);
      TextView a=text(money(o.optDouble("amount",0)),13,MINT,true);a.setGravity(Gravity.RIGHT);
      r.addView(n,new LinearLayout.LayoutParams(0,dp(41),1));
      r.addView(a,new LinearLayout.LayoutParams(-2,dp(41)));
      Button del=button("×",()->removeExpense(idx),false);
      LinearLayout.LayoutParams dl=new LinearLayout.LayoutParams(dp(43),dp(37));dl.leftMargin=dp(8);
      r.addView(del,dl);
      list.addView(r);line(list,0xff28373a);
    }
    marginBottom(page,text("Кредиты учитываются отдельно — без двойного списания.",12,MUTED,false),8);
  }
  private void goalScreen(){
    head("Планирование","Мои цели","Крупные покупки — под контролем.");
    marginBottom(page,button("+ Добавить цель",()->addGoal(),true),16);
    if(goals.length()==0){marginBottom(page,text("Пока нет финансовых целей.",13,MUTED,false),12);}
    for(int i=0;i<goals.length();i++){
      JSONObject o=goals.optJSONObject(i);if(o==null)continue;
      final int idx=i;
      LinearLayout c=section(o.optString("name","Цель"));
      double target=Math.max(0,o.optDouble("target",0)),saved=Math.max(0,o.optDouble("saved",0));
      rowInfo(c,"Накоплено",money(saved),MINT);rowInfo(c,"Нужно",money(target),WHITE);
      TextView progress=text((target>0?Math.round(Math.min(1,saved/target)*100):0)+"% выполнено",12,GREEN,true);
      marginBottom(c,progress,11);
      View bar=new View(this);bar.setBackground(bg(0xff2b3f3f,5));
      c.addView(bar,new LinearLayout.LayoutParams(-1,dp(8)));
      LinearLayout tools=horizontal();
      Button plus=button("+ Пополнить",()->addToGoal(idx),true);
      Button del=button("Удалить",()->removeGoal(idx),false);
      tools.addView(plus,new LinearLayout.LayoutParams(0,dp(43),1));
      LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(43),1);lp.leftMargin=dp(9);tools.addView(del,lp);
      marginBottom(c,tools,3);
    }
    LinearLayout reserve=section("Финансовая подушка");
    rowInfo(reserve,"Свободные накопления",money(savings),MINT);
    double essential=expenseTotal()+loans;
    rowInfo(reserve,"Запас в месяцах",essential>0?nf.format(savings/essential):"∞",WHITE);
    marginBottom(reserve,text("Ориентир: резерв на 3–6 месяцев регулярных платежей.",11,MUTED,false),4);
  }
  private String adviceText(){
    if(income<=0)return "Добавьте ежемесячный доход, чтобы увидеть рекомендации.";
    if(free()<0)return "Бюджет в минусе. Уменьшите необязательные расходы минимум на "+money(-free())+".";
    if(savings<3*(expenseTotal()+loans))return "Приоритет — финансовая подушка на 3 месяца обязательных платежей.";
    if(free()<income*.15)return "Свободных средств немного. Проверьте необязательные покупки.";
    return "Есть положительный остаток. Часть средств можно направлять в резерв и на финансовые цели.";
  }
  private void adviceScreen(){
    head("Финансовый анализ","Рекомендации","Конкретные действия без сложных терминов.");
    LinearLayout c=section("Состояние бюджета");
    rowInfo(c,"Ежемесячный доход",money(income),MINT);
    rowInfo(c,"Расходы и кредиты",money(expenseTotal()+loans),WHITE);
    rowInfo(c,"Свободно",money(free()),free()<0?RED:MINT);
    line(c,0xff314345);
    marginBottom(c,text(adviceText(),13,WHITE,false),3);
    LinearLayout s=section("Если сократить расходы");
    for(int pct:new int[]{5,10,15}){
      double saving=expenseTotal()*pct/100.0;
      rowInfo(s,"Сократить на "+pct+"%",money(saving)+" / мес.",MINT);
      marginBottom(s,text("Дополнительно за год: "+money(saving*12),11,MUTED,false),9);
    }
    LinearLayout purchase=section("Проверка покупки");
    marginBottom(purchase,text("Если потратить 15 000 ₽ прямо сейчас:",13,WHITE,false),8);
    double left=free()-15000;
    marginBottom(purchase,text(left>=0?"После покупки останется "+money(left)+" в месячном бюджете.":"Покупка создаст дефицит "+money(-left)+".",14,left>=0?MINT:RED,true),2);
    LinearLayout privacy=section("Конфиденциальность");
    marginBottom(privacy,text("Все данные хранятся только на телефоне. Интернет и регистрация не требуются.",12,MUTED,false),8);
    privacy.addView(button("Сбросить демонстрационные суммы",()->{
      new AlertDialog.Builder(this).setTitle("Сбросить данные?")
        .setMessage("Все изменения будут удалены.").setNegativeButton("Отмена",null)
        .setPositiveButton("Сбросить",(d,w)->{prefs.edit().clear().apply();load();render();}).show();
    },false));
  }
  private EditText entry(String hint,String value,boolean number){
    EditText f=new EditText(this);f.setText(value);f.setHint(hint);f.setTextColor(WHITE);
    f.setHintTextColor(MUTED);f.setSingleLine(true);f.setTextSize(17);
    if(number)f.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL|android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
    f.setBackground(bg(0xff253438,12));pad(f,13,12);return f;
  }
  private void field(LinearLayout body,String label,EditText edit){
    marginBottom(body,text(label,12,MUTED,false),6);marginBottom(body,edit,14);
  }
  private double parse(EditText e){
    try{return Math.max(0,Double.parseDouble(e.getText().toString().trim().replace(",",".")));}
    catch(Exception ex){return -1;}
  }
  private void settings(){
    LinearLayout form=vertical();pad(form,20,13);
    EditText i=entry("Доход",String.valueOf((long)income),true),l=entry("Кредиты",String.valueOf((long)loans),true),s=entry("Накопления",String.valueOf((long)savings),true);
    field(form,"Доход за месяц, ₽",i);field(form,"Платежи по кредитам в месяц, ₽",l);field(form,"Сбережения сейчас, ₽",s);
    new AlertDialog.Builder(this).setTitle("Настройки бюджета").setView(form).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{
      double a=parse(i),b=parse(l),c=parse(s);
      if(a<0||b<0||c<0){toast("Введите корректные суммы");return;}
      income=a;loans=b;savings=c;save();render();
    }).show();
  }
  private void addExpense(){
    LinearLayout form=vertical();pad(form,20,13);
    EditText n=entry("Например, продукты","",false),a=entry("Сумма","",true);
    field(form,"Название расхода",n);field(form,"Сумма в месяц, ₽",a);
    new AlertDialog.Builder(this).setTitle("Новый расход").setView(form).setNegativeButton("Отмена",null).setPositiveButton("Добавить",(d,w)->{
      double val=parse(a);String name=n.getText().toString().trim();
      if(name.isEmpty()||val<0){toast("Проверьте поля");return;}
      try{addJson(expenses,name,val);}catch(Exception ignored){}
      save();render();
    }).show();
  }
  private void removeExpense(int idx){
    JSONArray result=new JSONArray();
    for(int i=0;i<expenses.length();i++)if(i!=idx)result.put(expenses.opt(i));
    expenses=result;save();render();
  }
  private void addGoal(){
    LinearLayout form=vertical();pad(form,20,13);
    EditText n=entry("Например, отпуск","",false),a=entry("Стоимость","",true),s=entry("Уже накоплено","0",true);
    field(form,"Название цели",n);field(form,"Необходимо, ₽",a);field(form,"Накоплено, ₽",s);
    new AlertDialog.Builder(this).setTitle("Новая цель").setView(form).setNegativeButton("Отмена",null).setPositiveButton("Добавить",(d,w)->{
      double target=parse(a),saved=parse(s);String name=n.getText().toString().trim();
      if(name.isEmpty()||target<=0||saved<0){toast("Проверьте поля");return;}
      try{JSONObject o=new JSONObject();o.put("name",name);o.put("target",target);o.put("saved",saved);goals.put(o);}catch(Exception ignored){}
      save();render();
    }).show();
  }
  private void addToGoal(int idx){
    EditText amt=entry("Сумма пополнения","",true);pad(amt,12,13);
    new AlertDialog.Builder(this).setTitle("Пополнить цель").setView(amt)
      .setNegativeButton("Отмена",null).setPositiveButton("Добавить",(d,w)->{
        double x=parse(amt);if(x<=0){toast("Введите сумму");return;}
        JSONObject o=goals.optJSONObject(idx);
        if(o!=null){try{o.put("saved",o.optDouble("saved")+x);}catch(Exception ignored){}}
        save();render();
      }).show();
  }
  private void removeGoal(int idx){
    JSONArray result=new JSONArray();
    for(int i=0;i<goals.length();i++)if(i!=idx)result.put(goals.opt(i));
    goals=result;save();render();
  }
  private void toast(String text){Toast.makeText(this,text,Toast.LENGTH_SHORT).show();}
  private class DonutView extends View {
    double a,b,c;
    Paint p=new Paint(3);
    DonutView(Context ctx,double a,double b,double c){super(ctx);this.a=a;this.b=b;this.c=c;}
    @Override protected void onDraw(Canvas canvas){
      super.onDraw(canvas);float density=getResources().getDisplayMetrics().density;
      float x=getWidth()/2f,y=getHeight()/2f-2*density,r=Math.min(getWidth(),getHeight())*.39f;
      RectF oval=new RectF(x-r,y-r,x+r,y+r);
      double total=Math.max(.01,a+b+c);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(24));p.setStrokeCap(Paint.Cap.BUTT);
      p.setColor(0xff304247);canvas.drawArc(oval,0,360,false,p);
      float start=-90;
      for(int j=0;j<3;j++){
        double part=j==0?a:j==1?b:c;
        float sweep=(float)Math.max(0,part/total*360);
        p.setColor(j==0?GREEN:j==1?BLUE:0xff465b61);
        canvas.drawArc(oval,start,sweep,false,p);start+=sweep;
      }
      p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);p.setColor(WHITE);
      p.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));p.setTextSize(dp(22));
      canvas.drawText(String.valueOf((int)Math.round(Math.max(0,c/total*100)))+"%",x,y+dp(5),p);
      p.setColor(MUTED);p.setTextSize(dp(11));p.setTypeface(Typeface.DEFAULT);
      canvas.drawText("свободно",x,y+dp(24),p);
    }
  }
  private class ForecastView extends View{
    double start,monthly;Paint p=new Paint(3);
    ForecastView(Context c,double s,double m){super(c);start=s;monthly=m;}
    @Override protected void onDraw(Canvas cv){
      super.onDraw(cv);
      float left=dp(20),right=getWidth()-dp(20),top=dp(18),bottom=getHeight()-dp(30);
      double[] vals=new double[7];double min=Double.MAX_VALUE,max=-Double.MAX_VALUE;
      for(int i=0;i<7;i++){vals[i]=start+monthly*i;min=Math.min(min,vals[i]);max=Math.max(max,vals[i]);}
      if(max-min<1){max+=1;min-=1;}
      p.setColor(0xff304448);p.setStrokeWidth(dp(1));
      for(int i=0;i<4;i++){float y=top+(bottom-top)*i/3f;cv.drawLine(left,y,right,y,p);}
      Path path=new Path();
      for(int i=0;i<7;i++){
        float x=left+(right-left)*i/6f,y=bottom-(float)((vals[i]-min)/(max-min))*(bottom-top);
        if(i==0)path.moveTo(x,y);else path.lineTo(x,y);
      }
      p.setColor(monthly<0?RED:MINT);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));
      cv.drawPath(path,p);p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(10));
      for(int i=0;i<7;i++){float x=left+(right-left)*i/6f,y=bottom-(float)((vals[i]-min)/(max-min))*(bottom-top);
        cv.drawCircle(x,y,dp(3.2f),p);p.setColor(MUTED);p.setTextAlign(Paint.Align.CENTER);
        cv.drawText(String.valueOf(i),x,bottom+dp(18),p);p.setColor(monthly<0?RED:MINT);
      }
    }
  }
}
