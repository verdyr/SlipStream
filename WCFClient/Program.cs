using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using WcfService1;
using System.ServiceModel;
using System.Runtime.Remoting.Messaging;
using System.Reflection;
using System.Threading;

namespace WCFClient
{
    /// <summary>
    /// This is a sample class which is used to demonstrate the usage of the proxy class.
    /// </summary>
    class Program
    {
        /// <summary>
        /// This is the method for main code entry.
        /// </summary>
        /// <param name="args">The command line args for the code.</param>
        static void Main(string[] args)
        {
            //The value in which the result will be stored
            string value = String.Empty;
            
            //using a proxy for the interface IService1 call the method GetData
            //call a sync method
            new LamdaProxyHelper<IService1>().Use(serviceProxy =>
                 {
                     //save the return value in value
                     value = serviceProxy.GetData(7);
                 }, "WCFEndPoint");//use the end point name WCFEndPoint for this proxy

            Console.WriteLine("The value is " + value);
            CompositeType compositeType = new CompositeType();
            compositeType.BoolValue = true;
            compositeType.StringValue = "Test String";
            CompositeType returnValue = null;
            new LamdaProxyHelper<IService1>().Use(serviceProxy =>
            {
                returnValue = serviceProxy.GetDataUsingDataContract(compositeType);
            }, "WCFEndPoint");
            Console.WriteLine("The value is bool value = " + returnValue.BoolValue + " and string Value = " + returnValue.StringValue);
            
            //using a proxy for the interface IService1 call the method GetData
            //call a async method            
            new LamdaProxyHelper<IService1>().UseAsync(serviceProxy =>
            {
                serviceProxy.GetData(5);
            }, "WCFEndPoint" //use the end point WCFEndPoint
            , AsyncResultCallBack //When the method is done call this method
            , Guid.NewGuid() //This is the Id to identify when callback happens in case many call backs take place.
            );

            new LamdaProxyHelper<IService1>().UseAsync(serviceProxy =>
            {
                serviceProxy.GetDataUsingDataContract(compositeType);
            }, "WCFEndPoint", AsyncResultCallBack, Guid.NewGuid());

            new LamdaProxyHelper<IService1>().UseAsyncWithReturnValue((proxy, obj) =>
            {
                //save the return value in value
                value = proxy.GetData(9);
                CallBackForReturnValueOfGetData(value, (Guid)obj);
            }, "WCFEndPoint",Guid.NewGuid());//use the end point name WCFEndPoint for this proxy

            compositeType = new CompositeType();
            compositeType.BoolValue = false;
            compositeType.StringValue = "A new string";
            new LamdaProxyHelper<IService1>().UseAsyncWithReturnValue((serviceProxy, obj) =>
            {
                CompositeType compositeTypeReturnValue = 
                    serviceProxy.GetDataUsingDataContract(compositeType);
                CallBackForGetDataUsingDataContract(compositeTypeReturnValue,(Guid)obj);
            }, "WCFEndPoint", Guid.NewGuid());

            Console.ReadLine();
        }

        static void CallBackForGetDataUsingDataContract(CompositeType compositeType,Guid id)
        {
            Console.WriteLine("The value is bool value = " + compositeType.BoolValue
                + " and string Value = " + compositeType.StringValue+" which was called on the Guid = " +id);
        }

        /// <summary>
        /// This is a type safe return method for getting the return value on a
        /// seperate thread. This is called when the method is actully invoked.
        /// </summary>
        /// <param name="returnValue">This is the return value</param>
        static void CallBackForReturnValueOfGetData(string returnValue,Guid id)
        {
            Console.WriteLine("The return value is =" + returnValue+" which was called on the Guid = " +id);
        }
        /// <summary>
        /// This is the method called whenTask is done
        /// </summary>
        /// <param name="ar">The result</param>
       static void AsyncResultCallBack(IAsyncResult ar)
        {
            Console.WriteLine("Completed execution of " + ar.AsyncState);
        }

    }
}
