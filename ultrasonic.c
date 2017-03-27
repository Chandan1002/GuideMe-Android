#include<pic.h>
#define _XTAL_FREQ 20000000                                       

void sci_puts(const char *ptr);  

unsigned char data;
bit flag;

void main()
{

  SYNC=0;CREN=1;TXEN=1;SPEN=1;BRGH=1;SPBRG=129;

  GIE=1;PEIE=1;RCIE=1;
  
  RC4=0;
  __delay_ms(500);
   RC4=1;
  __delay_ms(500);
   RC4=0;
  __delay_ms(500);
   RC4=1;
  __delay_ms(500);

  
  PORTB=0;
   sci_puts("hello");
  while(1)
  	{
  	  if(flag)
        { 
          flag=0;
		  TXREG=data;                                                       
		  while(!TRMT);                                               
         
         }
   } 
}
void sci_puts(const char *ptr)
{
	while(*ptr)
      {TXREG=*(ptr++);                                                 
       while(!TRMT);
       }
}
void interrupt isr()
 {

   if(RCIF)
    {
     flag=1;
      data=RCREG; 
    } 
 }
 
